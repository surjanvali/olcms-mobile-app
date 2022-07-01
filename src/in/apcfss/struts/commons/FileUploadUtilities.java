package in.apcfss.struts.commons;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.struts.upload.FormFile;

public class FileUploadUtilities {
	
	//File Sizes
	public final long KB512 = 524288;
	public final long KB256 = 262144;
	public final long KB128 = 131072;
	public final long MB1 = 1048576;
	public final long MB2 = 2097152;
	public final long MB200 = 209715200;
	
	
	public long minSize = 0;
	public long maxSize = MB200;
	public FileTypes[] accepted_formats = {FileTypes.jpg,FileTypes.pdf,FileTypes.jpeg};
	public String basePath = ApplicationVariables.contextPath;;
	
	
	private static String getFileExtension(String filename) {
		if (filename != null && !filename.equals("")) {
			 return filename.substring(filename.lastIndexOf(".") + 1,filename.length());
		}
		return null;
	}
	
	public boolean checkFileExtensions(String filename) {
		String extension = getFileExtension(filename); 
		if(extension != null && !"".equals(extension.trim())) {
			for(FileTypes f : accepted_formats) {
				if(extension.equalsIgnoreCase(f.toString())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean checkFileSize(FormFile form_file) {
		if(form_file != null && (form_file.getFileSize() > minSize && form_file.getFileSize() < maxSize)) {
			return true;
		}
		return false;
	}
	
	public boolean checkFileSize(FormFile form_file, long fileMaxSize) {
		if(form_file != null && (form_file.getFileSize() > minSize && form_file.getFileSize() < fileMaxSize)) {
			return true;
		}
		return false;
	}
	
	public String saveFile(FormFile form_file,String upload_to_path, String attachemnt_name){
		String uploadPath = null;
		try {
			if(basePath != null && !"".equals(basePath.trim()) && checkFileSize(form_file) == true) {
				
				String filename = form_file.getFileName();
				
				if(checkFileExtensions(filename) == true) {
					
					File upload_folder = new File(basePath + upload_to_path);
					if (!upload_folder.exists()) {
						upload_folder.mkdirs();
					}
					
					String file_name = null;
					if (upload_folder.exists()) {
						file_name = attachemnt_name + "." + getFileExtension(filename);
						File file = new File(basePath + upload_to_path, file_name);
						if (!file.exists()) {
							FileOutputStream fcf = new FileOutputStream(file);
							fcf.write(form_file.getFileData());
							fcf.flush();
							fcf.close();
						}else {
							file.delete();
							FileOutputStream fcf = new FileOutputStream(file);
							fcf.write(form_file.getFileData());
							fcf.flush();
							fcf.close();
						}
					}else {
						throw new Exception(upload_folder + " Folder Not Exists");
					}
					uploadPath = upload_to_path + file_name;
				}
			}
		}catch (Exception e) {
			uploadPath = null;
			e.printStackTrace();
		}
		return uploadPath;
	}
	
	
}
