package poseteam.pose.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//태그 분류 기능에 사용되는 객체
public class TagClassCount {
    private String tagName;
    private int Count = 0;

    @Override
    public String toString() {
        return "TagClassCount{" +
                "tagName='" + tagName + '\'' +
                ", Count=" + Count +
                '}';
    }
}
