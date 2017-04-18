package mtrovo;

public class FixedTimestampProvider implements TimestampProvider{
    public long now = 0;
    @Override
    public long now() {
        return now;
    }
}
