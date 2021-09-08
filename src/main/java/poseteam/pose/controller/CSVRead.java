package poseteam.pose.controller;

import poseteam.pose.domain.Img;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Date별 사진 분류를 위해 csv파일읽어 리스트를 만듬.
public class CSVRead {
    public List<Img> readCSV(String userID){
        //반환용 리스트
        List<Img> imgDate = new ArrayList<>();
        BufferedReader br = null;

        try{
            //csv 파일을 읽어옴.
            br = Files.newBufferedReader
                    (Paths.get("C:\\Users\\pc\\Desktop\\캡스톤\\pose\\pose\\src\\main\\resources\\static\\excel\\"+ userID +".csv"));
            String line = "";

            while((line = br.readLine()) != null){
                //CSV 1행을 저장하는 리스트
                List<String> tmpList = new ArrayList<>();

                String array[] = line.split(",");
                //배열에서 리스트 반환
                tmpList = Arrays.asList(array);

                String tag = "";
                String tFN = "";
                //#은 태그 정보를 뜻함. 태그 데이터들을 각 하나의 String으로 쪼갬.
                for (String s : tmpList) {
                    if(s.contains("#")){
                        String[] split = s.split("#");
                        for (String s1 : split) {
                            tag = tag + s1 + "";
                        }
                    }
                }

                //http를 포함하는 것은 insta 게시글의 url 정보
                String http = "";
                for (String s : tmpList) {
                    if(s.contains("http")){
                        String[] split = s.split("'");
                        for (String s1 : split) {
                            if(s1.contains("http")){
                                http = s1;
                            }
                        }
                    }
                    //userID를 포함하는 것은 서버 디렉토리에 저장된 사진의 상대경로임으로 해당값에서 사진파일이름을 추출
                    else if(s.contains(userID)){
                        String[] split = s.split("'");
                        for (String s1 : split) {
                            if(s1.contains(userID)){
                                String[] split1 = s1.split("/");

                                //Img 객체에 저장하여 리스트를 만듬.
                                Img img = new Img();
                                img.setInstaSrc(http);
                                img.setImgFileName(split1[4]);
                                img.setSrc(s1);
                                img.setImgTag(tag);
                                imgDate.add(img);
                            }
                        }
                    }
                }
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();}
        finally{
            try{
                if(br != null){
                    br.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return imgDate;
    }
}
