package poseteam.pose.controller;
import poseteam.pose.domain.TagClassCount;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//해시태그별 사진 분류를 위한 메서드
public class HashTagClass {
    public List<String> Tag(String userID){
        //반환용 리스트
        BufferedReader br = null;

        List<String> tagFileName = new ArrayList<>();
        List<String> tagResult = new ArrayList<>();

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
                //읽어온 데이터들 중해 Tag값만 저장
                for (String s : tmpList) {
                    if(s.contains("#")){
                        tagFileName.add(s);
                    }
                }

            }
            System.out.println(tagFileName);

            List<String> tagTemp = new ArrayList<>();
            //태그 데이터들을 각 하나의 String으로 쪼갬.
            for (String s : tagFileName) { //[#세나클, #엽기고양이, #엽기고양이, #세나클#고양이#검정색, #고양이#카페]
                String[] split = s.split("#");
                for (String s1 : split) {
                    if(!(s1.equals(""))) tagTemp.add(s1.trim());
                }
            }

            System.out.println("tagTemp");
            System.out.println(tagTemp);

            //중복값이 있으면 카운터를 올려서 TagClassCount객체에 저장
            List<TagClassCount> temp = new ArrayList<>();
            TagClassCount tagClassCount = new TagClassCount();
            tagClassCount.setTagName(tagTemp.get(0));
            tagClassCount.setCount(tagClassCount.getCount());
            temp.add(tagClassCount);

            for (String s : tagTemp) {
                boolean check = false;
                for (TagClassCount classCount : temp) {
                    if(classCount.getTagName().equals(s)){
                        classCount.setCount(classCount.getCount()+1);
                        check = false;
                        break;
                    }
                    else{
                        check = true;
                    }
                }
                if(check){
                    tagClassCount = new TagClassCount();
                    tagClassCount.setTagName(s);
                    tagClassCount.setCount(tagClassCount.getCount() + 1);
                    temp.add(tagClassCount);
                }
            }
            System.out.println("temp");
            System.out.println(temp);

            //중복태그 갯수가 2이상인 태그값만 사진 분류 페이지에 보여주기 위해 따로 값을 저장(나머지는 기타에 폴더에 보여줌.)
            for (TagClassCount classCount : temp) {
                if(classCount.getCount() >=2 ){
                    System.out.println(temp);
                    tagResult.add(classCount.getTagName());
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
        System.out.println(tagResult);
        return tagResult;
    }
}
