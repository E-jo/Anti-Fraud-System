package antifraud.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
public class UserDTO {
    private long id;
    private String name, username, role;

    //private List<String> roles;

    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.role = user.getUserRole().substring(user.getUserRole().indexOf('_') + 1);
        //Set<String> unsortedRoles = user.getUserRoles();
        //this.roles = asSortedList(unsortedRoles);
    }

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> collection) {
        List<T> list = new ArrayList<T>(collection);
        java.util.Collections.sort(list);
        return list;
    }
}
