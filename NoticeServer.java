package edu;

//주석처리 for 브랜치 테스트
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap; 
//멀티스레드 병렬 프로그래밍  시 부분잠금(lcok) 시켜서 보안성 우수 
//synchronizedMap과 차이점은 동기화는 하나의 스레드가 처리할때 전체 잠금이 발생해서 다른 스레드는 대기 타야함...그래서..!

public class NoticeServer 
{
	private final ServerSocket serverSocket; //final 제어자 값 변경 불가!
	private Map<Integer, Socket> clientSocketMap; 
	
	public NoticeServer(int port) throws IOException { //메소드
		this.serverSocket = new ServerSocket(port); //생성자
		this.clientSocketMap = new ConcurrentHashMap<Integer, Socket>(); //생성자
	}
	
	public void serverStart() 
	{
		new Thread( 		//멀티쓰레드 생성
			new Runnable() { 
			@Override
			public void run() 
			{
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						clientSocketMap.putIfAbsent(socket.hashCode(), socket); //키값이 존재하면 기존값 반환, 없으면 입력한 값 저장후 반환
						System.out.println("key"+clientSocketMap.get(socket.hashCode()));
						System.out.println("value"+clientSocketMap.get(socket));
						System.out.println(socket.getLocalPort());
					} 
					catch (IOException e) {
						e.printStackTrace(); //스택 구조 뿌리는것..cpu에 과부하감.. 남용금지
					}
				}
			}
		}//익명클래스 끝
			).start(); //
	}
	
	
	
	
	public void notifyMessage(String message) throws IOException //공지하는 메소드
	{
		for (Iterator<Integer> iter = this.clientSocketMap.keySet().iterator();
				//동적으로 (ip,port)를 받아와서 키값을 모르는데 인덱스도 안정해져있으니까 해쉬맵 클래스 내부구조의 셋 자료구조에 키를 보관한 객체가 존재하니까 키셋으로 가져오고.
				//iterator 인터페이스를 통해 순차 탐색
				iter.hasNext() ; ) //next()는 더 읽을게 없으면 NoSuchElementException을 내기때문에 hasNext()로 더 ㅇ릭게 있는지 검사부터..
		{
			Integer key = iter.next();
			Socket socket = clientSocketMap.get(key);
			
			if (socket.isClosed()) {
				clientSocketMap.remove(key);
				continue;
			}
			
			try {
				OutputStream outputStream = socket.getOutputStream();
				outputStream.write(message.getBytes());
				outputStream.write("\r\n".getBytes());
				outputStream.flush(); //출력스트림과 버퍼된 출력 바이트를 강제로 쓰게한다..
			} 
			catch (Exception e) {
				clientSocketMap.remove(key); 
				continue;
			}
		} //포문 실행끝
	}
	
	
	public static void main(String[] args) 
	{
		NoticeServer server = null;
		try {
			System.out.println(args[0]);
			server = new NoticeServer( Integer.parseInt(args[0]) ); //쓰레드니까 하나씩 꺼내서..new로 생성자 간접호출
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("서버생성에 실패");
			System.exit(0); //현재 실행하고 있는 프로세스 강제종료 역할. (0)은 정상종료의 경우, 비정상 종료이면 0외의 다른값준다
		}
		
		server.serverStart();
		
		System.out.println("======= 공지사항을 입력하세요 ==========");
		while (true) 
		{
			@SuppressWarnings("resource") //어노테이션 : 컴파일러가 해주는 경고 중 ()내용은 제외시키는 옵션.리소스는 닫기 가능 유형의 자원 사용에 관련된 경고 억제
			Scanner inputScan = new Scanner(System.in);
			
			String line = inputScan.nextLine();
			
			try {
				server.notifyMessage(line);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
}
