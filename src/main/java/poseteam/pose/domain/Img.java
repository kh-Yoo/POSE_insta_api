package poseteam.pose.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//사진 분류 기능에 사용되는 Img객체
public class Img {
    private String src;
    private String instaSrc;
    private String imgFileName;
    private String imgTag;

    @Override
    public String toString() {
        return "Img{" +
                "src='" + src + '\'' +
                ", instaSrc='" + instaSrc + '\'' +
                ", imgFileName='" + imgFileName + '\'' +
                ", imgTag='" + imgTag + '\'' +
                '}';
    }
}
