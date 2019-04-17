import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bouncycastle.crypto.digests.SHA256Digest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hdactech.command.HdacCommand;
import com.hdactech.command.HdacException;
import com.hdactech.object.StreamKeyItem;

public class Sample2 {

	private final static String A_address = "1a9G4bW3ZTucMwMw6CKGPKTjabY4Qk2KZo8iGg";
	private final static String A_publickey = "029e81848b4041fbab717d80b675838676f0e6172843749e55ba018d793fe0e313";
	private final static String A_privatekey = "VCPHFbvjUWwNogUmfLjbT1nw3RFLkHA6akCZ17WANx7sUY1MHjuJN1bR";

	private final static String B_address = "1b6wzk2CQyKfpnJNeVGGX5u69f78jaZ51cqFtR";
	private final static String B_publickey = "030753be5811311f92c323155ae36202657caf05dc5a4d937e48b10f884902ebbc";
	private final static String B_privatekey = "VGibbe25tvJyBbJFtNqx4ShnS3KQBv5vZmrvh4g8Ffzdo7peM2eFiJBL";

	public static void main(String[] args) throws HdacException {
		HdacCommand cmd = new HdacCommand("191.1.11.125", "5756", "hdacrpc", "hdac1234");

		// ��༭ ���� ���� ����
		String contractHash = getFileHash("halla-01-001.txt");
		System.out.println("���ڰ�༭ ���� : " + contractHash);
		// A�� ���� ����
		String a_sign = cmd.getMessagingCommand().signMessage(A_privatekey, contractHash);
		System.out.println("A���� : " + a_sign);
		// B�� ���� ����
		String b_sign = cmd.getMessagingCommand().signMessage(B_privatekey, contractHash);
		System.out.println("B���� : " + b_sign);
		
		// JSON ������ �����
		JsonObject contractObject = new JsonObject();
		contractObject.addProperty("filehash", contractHash);
		contractObject.addProperty("sign-A", a_sign);
		contractObject.addProperty("sign-B", b_sign);

		String strContract = contractObject.toString();
		System.out.println("json data : " + strContract);
		
		// Hex ���ڿ��� ����
		String hexContract = bytesToHex(strContract.getBytes(StandardCharsets.UTF_8));
		System.out.println("hex data : " + hexContract);
		
		// Stream�� ���� "e-contract", "halla-001-01", json data
		String txid = cmd.getStreamCommand().publish("e-contract", "halla-01-001", hexContract);
		System.out.println("Result txid : " + txid);

		// Stream ��ȸ
		List<StreamKeyItem> streamkeyItem = cmd.getStreamCommand().listStreamKeyItems("e-contract", "halla-01-001");
		
		String key = streamkeyItem.get(0).getKey();
		String data = streamkeyItem.get(0).getData();
		
		System.out.println("key : " + key);
		System.out.println("data : " + hexToString(data));
		
		// JsonData Parsing
		JsonParser jParser = new JsonParser();
		JsonObject jObj = jParser.parse(hexToString(data)).getAsJsonObject();
		
		String filehash = jObj.get("filehash").getAsString();
		String signA = jObj.get("sign-A").getAsString();
		String signB = jObj.get("sign-B").getAsString();
		
		System.out.println("filehash : " + filehash);
		System.out.println("sign-A : " + signA);
		System.out.println("sign-B : " + signB);
		
		//��༭ ����
		if(contractHash.equals(filehash)) {
			System.out.println("��༭�� ���� ���� �ʾҽ��ϴ�.");
		} else {
			System.out.println("��༭�� ���� �Ǿ����ϴ�.");
		}
		
		//���� ����
		boolean verifyA = cmd.getMessagingCommand().verifyMessage(A_address, signA, contractHash);
		boolean verifyB = cmd.getMessagingCommand().verifyMessage(B_address, signB, contractHash);
		
		if(verifyA) {
			System.out.println("a�� ���� ������ �����Ͽ����ϴ�.");
		} else {
			System.out.println("a�� ���� ������ �����Ͽ����ϴ�.");
		}
		
		if(verifyB) {
			System.out.println("B�� ���� ������ �����Ͽ����ϴ�.");
		} else {
			System.out.println("B�� ���� ������ �����Ͽ����ϴ�.");
		}
	}
	
	

	private static String getFileHash(String filePath) {

		File file = new File(filePath);
		Scanner scan = null;
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		List<Byte> bFile = new ArrayList<>();

		StringBuffer sb = new StringBuffer();
		while (scan.hasNext()) {
			sb.append(scan.nextLine());
		}

		byte[] docHash = sha256(sb.toString().getBytes());

		return bytesToHex(docHash);
	}

	private static byte[] sha256(byte[] buf) {
		SHA256Digest digest = new SHA256Digest();
		byte[] byteData = new byte[digest.getDigestSize()];
		digest.update(buf, 0, buf.length);
		digest.doFinal(byteData, 0);

		return byteData;
	}

	private static String bytesToHex(byte[] bytes) {
		return new java.math.BigInteger(bytes).toString(16);
	}

	private static String hexToString(String strHex) {
		byte[] bytes = new java.math.BigInteger(strHex, 16).toByteArray();
		return new String(bytes);
	}
}
