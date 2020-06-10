package net.runelite.client.plugins.socket;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SocketAESEncryption
{

	public static String encrypt(String secret, String strToEncrypt)
	{
		try
		{
			byte[] key = secret.getBytes(StandardCharsets.UTF_8);

			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);

			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);

			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static String decrypt(String secret, String strToDecrypt)
	{
		try
		{
			byte[] key = secret.getBytes(StandardCharsets.UTF_8);

			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);

			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
