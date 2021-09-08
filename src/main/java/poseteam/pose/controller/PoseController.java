package poseteam.pose.controller;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import poseteam.pose.domain.Img;

import java.util.List;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PoseController {

    //사진 분류 기능을 위한 파이썬 파일 실행
    public void pyImgClassification(String userID, String userPW){
        ArrayList<String> list = new ArrayList<>();
        ProcessBuilder pb = null;
        Process process = null;
        BufferedReader reader = null;

        //해당 유저의 ID, PW를 파이썬 코드쪽으로 전송
        String id = userID;
        String pw = userPW;
        try {
            //다중사용자가 동시에 사용가능하도록 Process를 사용
            pb = new ProcessBuilder("python", "C:\\Users\\pc\\Desktop\\캡스톤\\python\\pyImgClassification.py");
            System.out.println(" 프로세스 시작");
            process = pb.start();

            //소켓 실행
            DataTransToPythonSocket.ConnectSocket();

            //OutputStream을 통해 socket으로 데이터(ID, PW)를 전달함.
            OutputStream output = DataTransToPythonSocket.socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(id + " " + pw + " ");
            //DataTransToPythonSocket.socket.close();

            //파이썬코드는 CMD창에서 동작하므로 결과값을 print하게 하였고, print한 값을 BufferedReader를 통해 가져온다.
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            //파이썬 코드의 print한 결과값이 끝날 때 같이 계속 값을 가져온다.
            while ((line = reader.readLine()) != null) {
                list.add(line);
                //결과값이 end가 들어오면 끝냄
                if(line.contains("end")){
                    System.out.println("end 들어옴");
                    break;
                }
            }
            process.waitFor(); //프로세스 끝날 때까지 대기

            //해당 동작을 완료하면 process를 제거해준다, BufferedReader도 닫아준다.
        } catch (IOException | InterruptedException e) {
            process.destroy();
            System.out.println(" exception " + e.getLocalizedMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            process.destroy();
        }
    }


    //이미지 태그 추천을 위한 객체로 추천태그값과 분류제목을 포함한다.
    class HashTag{
        String tag;
        String title;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "HashTag{" +
                    "tag='" + tag + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    //HashTag 객체를 통해 여러 추천태그값들을 리스트로 저장하여 return 하는 메서드이다.
    public ArrayList<HashTag> ImgRecommendationHashTag(String userID){
        ArrayList<HashTag> list = new ArrayList<>();
        Process process = null;
        //String result = null;

        String id = userID;

        StringBuffer buffer;
        //BufferedReader bufferedReader;
        BufferedReader reader = null;

            try {
                buffer = new StringBuffer();

                //cmd창으로 해시태그 추천기능을 실행하는 파이썬코드를 동작시킨다.
                buffer.append("cmd.exe ");
                buffer.append("/c ");
                buffer.append("C:\\Users\\pc\\Desktop\\캡스톤\\python\\venv\\Scripts\\python.exe C:/Users/pc/Desktop/캡스톤/python/predict.py");

                process = Runtime.getRuntime().exec(buffer.toString());

                //소켓 생성
                DataTransToPythonSocket.ConnectSocket();

                //파이썬쪽으로 해당 유저의 아이디값(추천을 원하는 사진 파일이 유저 아이디로 생성되기 때문에)을 넘긴다.
                OutputStream output = DataTransToPythonSocket.socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                String temp = "123";
                writer.println(id + " " + temp + " ");

                //InputStreamReader를 통해 파이썬 결과값을 가져온다.
                reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "MS949"));
                String line;


                String title= null;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if(line.contains("end")){
                        System.out.println("end 들어옴");
                        break;
                    }
                    //원하는 사진을 분석하여 나온 title값을 저장
                    if(line.contains("title : ")){
                        title = line.split("title : ")[1];
                        continue;
                    }
                    //넘어온 tag값을 HashTag 객체로 만들어 리스트에 저장
                    HashTag hashTag = new HashTag();
                    hashTag.setTag(line);
                    hashTag.setTitle(title);
                    list.add(hashTag);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        return list;
    }


    //-------------------------------------------------------------------------------------------------------------------------//
    //-------------------------------------------------------------------------------------------------------------------------//


    //로딩 메인 화면으로, 해시태그 추천기능 / 사진 분류 기능 / 인스타그램 모바일 버전 창 띄우기 기능을 포함하고 있다.
    @RequestMapping(value="/loading", method=RequestMethod.POST)
    public String Loading(@RequestParam("id") String userID, @RequestParam("password") String userPW, Model model){
        model.addAttribute("userID", userID);
        model.addAttribute("userPW", userPW);
        model.addAttribute("ServerAdd", ServerAdd.serverAdd);
        return "Background/loading";
    }

    //인스타그램 모바일 버전 창 띄우기 기능(로딩 메인 화면에서 버튼을 클릭하여 동작)
    @RequestMapping(value="/instaStart", method=RequestMethod.GET)
    public String InstaStart(){
        //다중 사용자를 위하여 크롬드라이버를 멀티쓰레드로 동작 시킨다.
        ChromeDriver_MultiThread cm  = new ChromeDriver_MultiThread();
        cm.run();
        return "Background/instaStart";
    }


    //-------------------------------------------------------------------------------------------------------------------------//
    //-------------------------------------------------------------------------------------------------------------------------//


    //해시태그 추천 기능 (로딩 메인 화면에서 버튼을 클릭하여 동작)
    @RequestMapping(value="/detailsPage", method=RequestMethod.POST)
    public String DetailsPage(@RequestParam("id") String userID, Model model){
        model.addAttribute("userID", userID);
        model.addAttribute("ServerAdd", ServerAdd.serverAdd);
        return "Background/detail";
    }

    // 해시태그 추천 기능으로 원하는 사진을 서버로 업로드하여 해당 사진을 유저 이름으로 서버 디렉토리에 저장한다.
    @RequestMapping(value="/load", method=RequestMethod.POST)                                                            //IOException - 파일이 없을 때 발생할 에러. 호출함수인 xml의 DispatcherServlet class로 예외처리 전가
    public String uploadSingle(@RequestParam("id") String userID, @RequestParam("files") MultipartFile report, Model model) throws IOException {    //command객체가 아닌 request로 submit한 값 받아오기     //studentNumber - submissionForm의 속성 name
        //파일명
        String originalFile = report.getOriginalFilename();
        //파일명 중 확장자만 추출                                                //lastIndexOf(".") - 뒤에 있는 . 의 index번호
        String originalFileExtension = originalFile.substring(originalFile.lastIndexOf("."));
        //fileuploadtest.doc
        //lastIndexOf(".") = 14(index는 0번부터)
        //substring(14) = .doc

        //업무에서 사용하는 리눅스, UNIX는 한글지원이 안 되는 운영체제
        //파일업로드시 파일명은 ASCII코드로 저장되므로, 한글명으로 저장 필요
        //UUID클래스 - (특수문자를 포함한)문자를 랜덤으로 생성                    "-"라면 생략으로 대체

        System.out.println(userID);
        //String storedFileName = UUID.randomUUID().toString().replaceAll("-", "") + originalFileExtension;
        String storedFileName = userID + originalFileExtension;


        //파일을 저장하기 위한 파일 객체 생성
        String basePath = "C:\\Users\\pc\\Desktop\\캡스톤\\pose\\pose\\src\\main\\resources\\static\\images\\downloadImg";
        //String filePath = basePath + "/" + file.getOriginalFilename();
        String filePath = basePath + "/";
        File file = new File(filePath + storedFileName);
        //파일 저장
        report.transferTo(file);

        System.out.println("가 업로드한 파일은");
        System.out.println(originalFile + "은 업로드한 파일이다.");
        System.out.println(storedFileName + "라는 이름으로 업로드 됐다.");
        System.out.println("파일사이즈는 " + report.getSize());

        model.addAttribute("userID", userID);
        model.addAttribute("ServerAdd", ServerAdd.serverAdd);
        return "Background/detailPageLoading";
    }

    //해시태그 추천 기능으로, 업로드 된 사진을 분석하여 해시태그를 추천하기 위한 ImgRecommendationHashTag 메소드를 동작시킨다.
    @RequestMapping(value="/imageTagRecommend", method=RequestMethod.POST)
    public String TagClass(@RequestParam("id") String userID, Model model){
        ArrayList<HashTag> result = ImgRecommendationHashTag(userID);
        System.out.println(result);

        String title = result.get(0).getTitle();

        model.addAttribute("title", title);
        model.addAttribute("userID", userID);
        model.addAttribute("result", result);
        return "Background/showRecommendTag"; //여기서 tag 띄워져야함.
    }


    //-------------------------------------------------------------------------------------------------------------------------//
    //-------------------------------------------------------------------------------------------------------------------------//


    // 사진 분류 기능으로, 실행 로딩 페이지를 띄워줌.
    @RequestMapping(value="/dateImgCrawlingLoading", method=RequestMethod.POST)
    public String dateImgCrawlingLoading(@RequestParam("id") String userID, @RequestParam("pw") String userPW, Model model){
        model.addAttribute("userID", userID);
        model.addAttribute("userPW", userPW);
        model.addAttribute("ServerAdd", ServerAdd.serverAdd);
        return "Background/dateImgCrawlingloading";
    }

    //사진 분류 기능으로, 해당 유저의 인스타 계정에 접근해 사진과 게시글 정보를 크롤링 해온다. / DATE별, TAG별 사진 분류 기능
    @RequestMapping(value="/imageSortDate", method=RequestMethod.POST)
    public String ImageSortDate(@RequestParam("id") String userID, @RequestParam("pw") String userPW,Model model){

        //파이썬 코드 삽입 (Date), 인스타 계정에 접근하여 데이터를 크롤링해옴.
        //사진을 서버의 디렉토리에 저장, 게시글 정보는 csv파일로 저장
        pyImgClassification(userID,userPW);

        //csv파일 읽기, 유저의 게시글 정보를 읽어옴 -> DATE별 사진 분류를 위해 사용됨.
        CSVRead csvRead = new CSVRead();
        List<Img> imgSrcDate = csvRead.readCSV(userID);

        System.out.println("이미지 src data \n");
        System.out.println(imgSrcDate);

        //TAG별 분류를 위해 csv를 읽어 리스트를 만듬.
        System.out.println("태그 분류");
        HashTagClass hashTagClass = new HashTagClass();
        List<String> tag = hashTagClass.Tag(userID);

        model.addAttribute("hashTagClass", tag);
        model.addAttribute("imgSrcDate", imgSrcDate);
        model.addAttribute("userID", userID);
        model.addAttribute("ServerAdd", ServerAdd.serverAdd);

        return "Background/index_main";
    }

    //사진 분류 기능으로, Date별 사진 분류에서 년도를 파악하여 데이터를 보여줌.
    @RequestMapping(value="/gallery", method=RequestMethod.GET)
    public String Gallery(@RequestParam("id") String userID, @RequestParam("year") String year, Model model){
        //csv파일 읽기
        CSVRead csvRead = new CSVRead();
        List<Img> imgSrcDate = csvRead.readCSV(userID);

        model.addAttribute("imgSrcDate", imgSrcDate);
        model.addAttribute("year", year);
        model.addAttribute("userID", userID);
        model.addAttribute("ServerAdd", ServerAdd.serverAdd);

        return "Background/index_gallery";
    }

    //사진 분류 기능으로, Date별 사진 분류에서 년도와 월을 파악하여 사진을 보여줌.
    @RequestMapping(value="/showPhoto", method=RequestMethod.GET)
    public String ShowPhoto(@RequestParam("id") String userID, @RequestParam("year") String year, @RequestParam("month") String month,Model model){
        CSVRead csvRead = new CSVRead();
        List<Img> imgSrcDate = csvRead.readCSV(userID);

        model.addAttribute("imgSrcDate", imgSrcDate);
        model.addAttribute("year", year);
        model.addAttribute("userID", userID);
        model.addAttribute("month", month);
        model.addAttribute("ServerAdd", ServerAdd.serverAdd);
        //System.out.println(year + month + userID);

        return "Background/gallery";
    }

    //사진 분류 기능으로, Tag별로 사진을 보여줌.
    @RequestMapping(value="/TagClass", method=RequestMethod.GET)
    public String TagClass(@RequestParam("id") String userID, @RequestParam("tagName") String tagName, Model model){
        //csv파일 읽기
        CSVRead csvRead = new CSVRead();
        List<Img> imgSrcDate = csvRead.readCSV(userID);

        HashTagClass hashTagClass = new HashTagClass();
        List<String> tag = hashTagClass.Tag(userID);

        model.addAttribute("hashTagClass", tag);
        model.addAttribute("imgSrcDate", imgSrcDate);
        model.addAttribute("userID", userID);
        model.addAttribute("tagName", tagName);
        model.addAttribute("ServerAdd", ServerAdd.serverAdd);

        return "Background/TagGallery";
    }
}

//-------------------------------------------------------------------------------------------------------------------------//
//-------------------------------------------------------------------------------------------------------------------------//

//크롬드라이버 멀티쓰레드로 사용하여 다중 사용자가 접근 가능하게 하는 메소드, 인스타그램을 모바일 버전으로 보여준다.
class ChromeDriver_MultiThread extends Thread
{
    //WebDriver 설정
    private WebDriver driver, driver2, driver3;
    private WebElement element;
    private String url = "https://www.instagram.com/";
    public void run() {
        //chromeOptions.addArguments("headless"); //안보이게 하는거

        Map<String, Object> deviceMetrics = new HashMap<>();
        deviceMetrics.put("width", 480);
        deviceMetrics.put("height", 520);
        deviceMetrics.put("pixelRatio", 3.0);

        Map<String, Object> mobileEmulation = new HashMap<String, Object>();
        mobileEmulation.put("deviceMetrics", deviceMetrics);

        mobileEmulation.put("userAgent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        chromeOptions.addArguments("--window-size=480,700");

        System.setProperty("webdriver.chrome.driver", "C:\\Users\\pc\\Desktop\\캡스톤\\chromedriver_win32\\chromedriver.exe");
        try{
            driver = new ChromeDriver(chromeOptions);
            driver.get(url);
        }catch (Exception e){
            System.out.println(e);
            driver.close();
        }
    }
}
