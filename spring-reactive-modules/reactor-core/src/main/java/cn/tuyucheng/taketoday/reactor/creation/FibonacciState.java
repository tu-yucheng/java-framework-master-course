package cn.tuyucheng.taketoday.reactor.creation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FibonacciState {
    private int former;
    private int latter;
}