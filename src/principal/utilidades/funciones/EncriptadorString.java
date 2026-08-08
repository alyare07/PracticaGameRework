package principal.utilidades.funciones;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncriptadorString {
	private final String LLAVE = "ESCALONETA";
	
	protected EncriptadorString() {
		
	}
	
	
	public String encriptar(String s) {
		return s;
	}
	
	public String desencriptar(String s) {
		return s;
	}
	

	// Clave de encriptación / desencriptación
    private SecretKeySpec CrearClave(String llave) {
        try {
            byte[] cadena = llave.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            cadena = md.digest(cadena);
            cadena = Arrays.copyOf(cadena, 16);
            SecretKeySpec secretKeySpec = new SecretKeySpec(cadena, "AES");
            return secretKeySpec;
        } catch (Exception e) {
            return null;
        }

    }

//    public String encriptar(String encriptar) {
//     
//        try {
//        SecretKeySpec secretKeySpec = CrearClave(LLAVE);
//            Cipher cipher = Cipher.getInstance("AES");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
//            
//            byte [] cadena = encriptar.getBytes("UTF-8");
//            byte [] encriptada = cipher.doFinal(cadena);
//            String cadena_encriptada =  Base64.getEncoder().encodeToString(encriptada);
//            return cadena_encriptada;
//            
//            
//            
//        } catch (Exception e) {
//            return "";
//        }
//    }
//
//     public String desencriptar(String desencriptar) {
//     System.out.println(desencriptar);
//        try {
//            SecretKeySpec secretKeySpec = CrearClave(LLAVE);
//            Cipher cipher = Cipher.getInstance("AES");
//            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
//            
//            byte [] cadena = Base64.getMimeDecoder().decode(desencriptar);
//            byte [] desencriptacioon = cipher.doFinal(cadena);
//            String cadena_desencriptada = new String(desencriptacioon);
//            return cadena_desencriptada;
//            
//        } catch (Exception e) {
//        	e.printStackTrace();
//            return "";
//        }
//    }

}
