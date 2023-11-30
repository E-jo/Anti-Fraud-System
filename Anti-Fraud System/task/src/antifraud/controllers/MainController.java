package antifraud.controllers;

import antifraud.models.*;
import antifraud.repositories.TransactionLimitsRepository;
import antifraud.services.StolenCardService;
import antifraud.services.SuspiciousIPService;
import antifraud.services.TransactionService;
import antifraud.services.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;


@RestController
public class MainController {

    private final String ip4RegEx = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
    @Autowired
    UserService userService;
    @Autowired
    SuspiciousIPService suspiciousIPService;
    @Autowired
    StolenCardService stolenCardService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    TransactionLimitsRepository transactionLimitsRepository;

    @PostMapping("/api/antifraud/transaction")
    public ResponseEntity<?> validateTransaction(@Valid @RequestBody Transaction transaction,
                                                 @AuthenticationPrincipal User user) {

     /*
        public ResponseEntity<?> validateTransaction(@RequestParam String amountStr,
                @AuthenticationPrincipal User user) {

        long amount = Long.parseLong(amountStr);
        TransactionResponse response = new TransactionResponse();
        response.setResult("ALLOWED");

     */
        TransactionLimits transactionLimits = transactionLimitsRepository.findFirstByOrderByIdDesc().get();

        List<String> infoReasons = new ArrayList<>();

        TransactionResponse response = new TransactionResponse();

        try {
            long amount = transaction.getAmount();
            if (amount <= 0) {
                throw new RuntimeException();
            }
            if (amount <= transactionLimits.getAllowedLimit()) {
                response.setResult("ALLOWED");
            } else if (amount <= transactionLimits.getManualLimit()) {
                response.setResult("MANUAL_PROCESSING");
                infoReasons.add("amount");
            } else {
                response.setResult("PROHIBITED");
                infoReasons.add("amount");
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (response.getResult().equalsIgnoreCase("ALLOWED")) {
            infoReasons.add("none");
        }

        Optional<StolenCard> optionalStolenCard =
                stolenCardService.findByNumber(transaction.getNumber());
        if (optionalStolenCard.isPresent()) {
            if (response.getResult().equalsIgnoreCase("MANUAL_PROCESSING")) {
                infoReasons.remove("amount");
            }
            response.setResult("PROHIBITED");
            infoReasons.add("card-number");
        }

        Optional<SuspiciousIP> optionalSuspiciousIP =
                suspiciousIPService.findByIp(transaction.getIp());
        if (optionalSuspiciousIP.isPresent()) {
            if (response.getResult().equalsIgnoreCase("MANUAL_PROCESSING")) {
                infoReasons.remove("amount");
            }
            response.setResult("PROHIBITED");
            infoReasons.add("ip");
        }

        LocalDateTime endTime = LocalDateTime.parse(transaction.getDate());
        LocalDateTime startTime = endTime.minusHours(1);

        if (transactionService.checkForTwoFromDifferentIP(transaction.getIp(),
                startTime, endTime)) {
            response.setResult("MANUAL_PROCESSING");
            infoReasons.add("ip-correlation");
        }

        if (transactionService.checkForMoreThanTwoFromDifferentIP(transaction.getIp(),
                startTime, endTime)) {
            response.setResult("PROHIBITED");
            infoReasons.add("ip-correlation");
        }

        if (transactionService.checkForTwoFromDifferentRegion(transaction.getRegion(),
                startTime, endTime)) {
            response.setResult("MANUAL_PROCESSING");
            infoReasons.add("region-correlation");
        }

        if (transactionService.checkForMoreThanTwoFromDifferentRegion(transaction.getRegion(),
                startTime, endTime)) {
            response.setResult("PROHIBITED");
            infoReasons.add("region-correlation");
        }

        if (!response.getResult().equalsIgnoreCase("ALLOWED")) {
            infoReasons.remove("none");
        }

        Collections.sort(infoReasons);
        StringBuilder info = new StringBuilder();
        for (int i = 0; i < infoReasons.size(); i++) {
            info.append(infoReasons.get(i));
            if (i != infoReasons.size() - 1) {
                info.append(", ");
            }
        }
        response.setInfo(info.toString());
        transaction.setResult(response.getResult());

        transactionService.save(transaction);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/auth/user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
/*
    public ResponseEntity<?> registerUser(@RequestParam String name,
                                          @RequestParam String username,
                                          @RequestParam String password) {


 */
        User newUser = new User();
        newUser.setName(user.getName());
        newUser.setUsername(user.getUsername());
        newUser.setPassword(new BCryptPasswordEncoder(13).encode(user.getPassword()));

        Optional<User> userOptional = userService.findByUsernameIgnoreCase(newUser.getUsername());
        if (userOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // check if it is the first user, if so give admin role otherwise merchant role
        if (userService.findAll().isEmpty()) {
            newUser.setUserRole("ROLE_ADMINISTRATOR");
            newUser.setAccountNonLocked(true);
        } else {
            newUser.setUserRole("ROLE_MERCHANT");
            newUser.setAccountNonLocked(false);
        }

        userService.save(newUser);

        return new ResponseEntity<>(new UserDTO(newUser), HttpStatus.CREATED);
    }

    @GetMapping("/api/auth/list")
    public ResponseEntity<?> listUsers(@AuthenticationPrincipal User requestingUser) {
        List<User> allUsers = userService.findAllByOrderByIdAsc();
        List<UserDTO> formattedUsers = new ArrayList<>();
        for (User user : allUsers) {
            formattedUsers.add(new UserDTO(user));
        }

        Gson gson = new Gson();
        String result = gson.toJson(formattedUsers);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/api/auth/user/{username}")
    public ResponseEntity<?> deleteUserByPathVar(@PathVariable String username,
                                        @AuthenticationPrincipal User user) {
        Optional<User> userOptional = userService.findByUsernameIgnoreCase(username);
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User userToDelete = userOptional.get();
        userService.delete(userToDelete);

        return new ResponseEntity<>(new UserDeletedMessage(username, "Deleted successfully!"), HttpStatus.OK);
    }

    @DeleteMapping("/api/auth/user/")
    public ResponseEntity<?> deleteUserByParam(@RequestParam String username,
                                        @AuthenticationPrincipal User user) {
        Optional<User> userOptional = userService.findByUsernameIgnoreCase(username);
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User userToDelete = userOptional.get();
        userService.delete(userToDelete);

        return new ResponseEntity<>(new UserDeletedMessage(username, "Deleted successfully!"), HttpStatus.OK);
    }

    @PutMapping("/api/auth/role")
    public ResponseEntity<?> changeRole(@AuthenticationPrincipal User user,
                                        @Valid @RequestBody RoleChangeRequest roleChangeRequest) {
        if (!roleChangeRequest.getRole().equalsIgnoreCase("SUPPORT") &&
            !roleChangeRequest.getRole().equalsIgnoreCase("MERCHANT")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<User> userOptional =
                userService.findByUsernameIgnoreCase(roleChangeRequest.getUsername());
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User userToChange = userOptional.get();
        String role = "ROLE_" + roleChangeRequest.getRole();
        if (userToChange.getUserRole().equalsIgnoreCase(role)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        userToChange.setUserRole(role);
        userService.save(userToChange);

        return new ResponseEntity<>(new UserDTO(userToChange), HttpStatus.OK);
    }

    @PutMapping({"/api/auth/access/", "/api/auth/access"})
    public ResponseEntity<?> changeUserLock(@Valid @RequestBody LockChangeRequest lockChangeRequest,
                                            @AuthenticationPrincipal User requestingUser) {
        Optional<User> userOptional =
                userService.findByUsernameIgnoreCase(lockChangeRequest.getUsername());
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = userOptional.get();
        String resultStr = "User " + user.getUsername();

        if (lockChangeRequest.getOperation().equalsIgnoreCase("LOCK")) {
            // cannot lock account with admin role
            if (user.getUserRole().equalsIgnoreCase("ROLE_ADMINISTRATOR")) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            user.setAccountNonLocked(false);
            resultStr += " locked!";
        } else if (lockChangeRequest.getOperation().equalsIgnoreCase("UNLOCK")) {
            user.setAccountNonLocked(true);
            //user.setFailedAttempt(0);
            resultStr += " unlocked!";
        }
        userService.save(user);

        JsonObject result = new JsonObject();
        result.addProperty("status", resultStr);

        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    @PostMapping("/api/antifraud/suspicious-ip")
    public ResponseEntity<?> addSuspiciousIP(@AuthenticationPrincipal User user,
                                             @RequestBody SuspiciousIP suspiciousIP) {
        Optional<SuspiciousIP> optionalSuspiciousIP =
                suspiciousIPService.findByIp(suspiciousIP.getIp());
        if (optionalSuspiciousIP.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (!suspiciousIP.getIp().matches(ip4RegEx)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        SuspiciousIP newSuspiciousIP = new SuspiciousIP();
        newSuspiciousIP.setIp(suspiciousIP.getIp());

        suspiciousIPService.save(newSuspiciousIP);

        return new ResponseEntity<>(newSuspiciousIP, HttpStatus.OK);
    }

    @DeleteMapping("/api/antifraud/suspicious-ip/{ip}")
    public ResponseEntity<?> deleteSuspiciousIP(@AuthenticationPrincipal User user,
                                                @PathVariable String ip) {
        if (!ip.matches(ip4RegEx)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<SuspiciousIP> optionalSuspiciousIP =
                suspiciousIPService.findByIp(ip);
        if (optionalSuspiciousIP.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        SuspiciousIP suspiciousIP = optionalSuspiciousIP.get();
        suspiciousIPService.delete(suspiciousIP);

        JsonObject result = new JsonObject();
        result.addProperty("status", "IP " + ip + " successfully removed!");

        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    @GetMapping("/api/antifraud/suspicious-ip")
    public ResponseEntity<?> getAllSuspiciousIP(@AuthenticationPrincipal User user) {
        List<SuspiciousIP> allSuspiciousIP = suspiciousIPService.findAllByOrderByIdAsc();

        Gson gson = new Gson();
        String result = gson.toJson(allSuspiciousIP);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/api/antifraud/stolencard")
    public ResponseEntity<?> addStolenCard(@AuthenticationPrincipal User user,
                                           @RequestBody StolenCard stolenCard) {
        Optional<StolenCard> optionalStolenCard = stolenCardService.findByNumber(stolenCard.getNumber());
        if (optionalStolenCard.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (!luhnCheck(stolenCard.getNumber())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        StolenCard newStolenCard = new StolenCard();
        newStolenCard.setNumber(stolenCard.getNumber());

        stolenCardService.save(newStolenCard);

        return new ResponseEntity<>(newStolenCard, HttpStatus.OK);
    }

    @DeleteMapping("/api/antifraud/stolencard/{number}")
    public ResponseEntity<?> deleteStolenCard(@AuthenticationPrincipal User user,
                                                @PathVariable String number) {
        if (!luhnCheck(number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<StolenCard> optionalStolenCard = stolenCardService.findByNumber(number);
        if (optionalStolenCard.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        StolenCard stolenCard = optionalStolenCard.get();


        stolenCardService.delete(stolenCard);

        JsonObject result = new JsonObject();
        result.addProperty("status", "Card " + number + " successfully removed!");

        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    @GetMapping("/api/antifraud/stolencard")
    public ResponseEntity<?> getAllStolenCard(@AuthenticationPrincipal User user) {
        List<StolenCard> allStolenCard = stolenCardService.findAllByOrderByIdAsc();

        Gson gson = new Gson();
        String result = gson.toJson(allStolenCard);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/api/antifraud/transaction")
    public ResponseEntity<?> addFeedback(@AuthenticationPrincipal User user,
                                         @RequestBody TransactionFeedbackRequest transactionFeedbackRequest) {
        Optional<Transaction> optionalTransaction =
                transactionService.findByTransactionId(transactionFeedbackRequest.getTransactionId());
        if (optionalTransaction.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check for allowed feedback strings
        if (!transactionFeedbackRequest.getFeedback().equalsIgnoreCase("ALLOWED") &&
                !transactionFeedbackRequest.getFeedback().equalsIgnoreCase("MANUAL_PROCESSING") &&
                !transactionFeedbackRequest.getFeedback().equalsIgnoreCase("PROHIBITED")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // check for existing feedback
        Transaction transaction = optionalTransaction.get();
        if (!transaction.getFeedback().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        TransactionLimits transactionLimits = transactionLimitsRepository.findFirstByOrderByIdDesc().get();

        // apply modifications as per table
        if (transaction.getResult().equalsIgnoreCase("ALLOWED")) {
            if (transactionFeedbackRequest.getFeedback().equalsIgnoreCase("ALLOWED")) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            if (transactionFeedbackRequest.getFeedback().equalsIgnoreCase("MANUAL_PROCESSING")) {
                transactionLimits.setAllowedLimit((int) Math.ceil(
                        (0.8 * transactionLimits.getAllowedLimit()) - (0.2 * transaction.getAmount())));
            }
            if (transactionFeedbackRequest.getFeedback().equalsIgnoreCase("PROHIBITED")) {
                transactionLimits.setAllowedLimit((int) Math.ceil(
                        (0.8 * transactionLimits.getAllowedLimit()) - (0.2 * transaction.getAmount())));
                transactionLimits.setManualLimit((int) Math.ceil(
                        (0.8 * transactionLimits.getManualLimit()) - (0.2 * transaction.getAmount())));            }
        }

        if (transaction.getResult().equalsIgnoreCase("MANUAL_PROCESSING")) {
            if (transactionFeedbackRequest.getFeedback().equalsIgnoreCase("ALLOWED")) {
                transactionLimits.setAllowedLimit((int) Math.ceil(
                        (0.8 * transactionLimits.getAllowedLimit()) + (0.2 * transaction.getAmount())));            }
            if (transactionFeedbackRequest.getFeedback().equalsIgnoreCase("MANUAL_PROCESSING")) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            if (transactionFeedbackRequest.getFeedback().equalsIgnoreCase("PROHIBITED")) {
                transactionLimits.setManualLimit((int) Math.ceil(
                        (0.8 * transactionLimits.getManualLimit()) - (0.2 * transaction.getAmount())));             }
        }

        if (transaction.getResult().equalsIgnoreCase("PROHIBITED")) {
            if (transactionFeedbackRequest.getFeedback().equalsIgnoreCase("ALLOWED")) {
                transactionLimits.setAllowedLimit((int) Math.ceil(
                        (0.8 * transactionLimits.getAllowedLimit()) + (0.2 * transaction.getAmount())));
                transactionLimits.setManualLimit((int) Math.ceil(
                        (0.8 * transactionLimits.getManualLimit()) + (0.2 * transaction.getAmount())));
            }
            if (transactionFeedbackRequest.getFeedback().equalsIgnoreCase("MANUAL_PROCESSING")) {
                transactionLimits.setManualLimit((int) Math.ceil(
                        (0.8 * transactionLimits.getManualLimit()) + (0.2 * transaction.getAmount())));
            }
            if (transactionFeedbackRequest.getFeedback().equalsIgnoreCase("PROHIBITED")) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        transaction.setFeedback(transactionFeedbackRequest.getFeedback());

        transactionService.save(transaction);
        transactionLimitsRepository.save(transactionLimits);

        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @GetMapping("api/antifraud/history")
    public ResponseEntity<?> getTransactionHistory(@AuthenticationPrincipal User user) {
        List<Transaction> allTransaction = transactionService.findAllByOrderByTransactionIdAsc();

        Gson gson = new Gson();
        String result = gson.toJson(allTransaction);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/api/antifraud/history/{number}")
    public ResponseEntity<?> getTransactionHistoryByCardNumber(@AuthenticationPrincipal User user,
                                                               @PathVariable String number) {
        if (!luhnCheck(number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<List<Transaction>> userHistory = transactionService.findAllByNumber(number);
        if (userHistory.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<Transaction> allUserTransactions = userHistory.get();
        if (allUserTransactions.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Gson gson = new Gson();
        String result = gson.toJson(allUserTransactions);

        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    private static boolean luhnCheck(String number) {
        List<Integer> numbers = new ArrayList<>(Arrays.stream(number.split(""))
                .map(Integer::parseInt)
                .toList());

        List<Integer> newNumbers = new ArrayList<>();
        for (int i = 0; i < numbers.size(); i++) {
            int num = numbers.get(i);
            if ((i + 1) % 2 != 0) {
                newNumbers.add(num * 2);
            } else {
                newNumbers.add(num);
            }
        }

        for (int i = 0; i < newNumbers.size(); i++) {
            int num = newNumbers.get(i);
            if (num > 9) {
                newNumbers.set(i, num - 9);
            }
        }

        int sum = newNumbers.stream().reduce(0, Integer::sum);
        return sum % 10 == 0;
    }

}
