package poseteam.pose.controller;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DataTransToPythonSocket {
    //static을 사용해서 서버는 공통으로 사용하게 함.
    public static ServerSocket serverSocket;
    public static Socket socket = null;
    static {
        try {
            serverSocket = new ServerSocket(8000);
            System.out.println("socket : " + 8000 + "으로 서버가 열렸습니다");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ConnectSocket(){
        // 소켓 서버가 종료될 때까지 반복
        try {
            DataTransToPythonSocket.socket = DataTransToPythonSocket.serverSocket.accept(); // 소켓 서버로 접속 시 socketUser에 접속자 정보 할당
            System.out.println("Client가 접속함 : " + DataTransToPythonSocket.socket.getLocalAddress()); // 접속자의 getLocalAddress 가져오기
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
