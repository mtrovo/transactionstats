package mtrovo;

import org.springframework.stereotype.Component;

@Component
public class SystemTimestampProvider implements TimestampProvider {
    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}
