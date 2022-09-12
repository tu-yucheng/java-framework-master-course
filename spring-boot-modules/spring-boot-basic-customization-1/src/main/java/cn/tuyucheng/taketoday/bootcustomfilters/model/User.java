package cn.tuyucheng.taketoday.bootcustomfilters.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * User model
 * @author tuyucheng
 */
@Data
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private String email;
}