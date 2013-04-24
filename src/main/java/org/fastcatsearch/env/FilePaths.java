package org.fastcatsearch.env;

import java.io.File;

public class FilePaths {
	
	private Environment environment;
	
	
	public FilePaths(Environment environment){
		this.environment = environment;
	}
	
	public String home() {
		return environment.home();
	}
	public File homeFile() {
		return environment.homeFile();
	}
	
	public String getCollectionHome(String collection) {
		return makePath("collection").append(collection).toString();
	}

	public String getCollectionDataPath(String collection) {
		return getCollectionDataPath(collection, 0);
	}

	public String getCollectionDataPath(String collection, int dataSequence) {
		if (dataSequence == 0)
			return makePath("collection").append(collection).append("data").toString();
		else
			return makePath("collection").append(collection).append("data"+dataSequence).toString();
	}

	public String getSegmentPath(String collection, int dataSequence, int segmentNumber) {
		return makePath(getCollectionDataPath(collection, dataSequence)).append(segmentNumber+"").toString();
	}
	
	public String getPath(String path) {
		if(path == null){
			path = home();
		}
		return getFile(path).getPath();
	}
	
	public File getFile(String path) {
		if(path == null){
			path = home();
		}
		
		if(Environment.OS_NAME.startsWith("Windows")){
			if(path.matches("^[a-zA-Z]:\\\\.*")){ // 윈도우즈는 c:\\와 같이 시작하지 않으면 상대경로이다.
				return new File(path);
			}
			if(path.matches("^[a-zA-Z]://.*")){ // c://와 같이 사용시를 고려.
				return new File(path);
			}
			if (path.startsWith("/")) {
				return new File(path);
			}
		}
		
		if (path.startsWith(Environment.FILE_SEPARATOR)) {
			return new File(path);
		}
		
		return new File(environment.homeFile(), path);
	}
	
	public File getRelativePathFile(File sourceFile) {
		String base = environment.home();
		String path = sourceFile.getPath();
		String relativePath = new File(base).toURI().relativize(new File(path).toURI()).getPath();
		return new File(relativePath);
	}
	
	public Path makePath(String path){
		if(path == null){
			return new Path(homeFile());
		}
		return new Path(getFile(path));
	}
	
	public Path makeRelativePath(String path){
		if(path == null){
			return new Path();
		}
		return new Path(null, path);
	}
	
	public class Path{
		private File root;
		public Path(){
		}
		public Path(File root){
			this.root = root;
		}
		public Path(File root, String sub){
			this.root = new File(root, sub);
		}
		public Path append(String dir){
			return new Path(root, dir);
		}
		public File file(){
			return root;
		}
		@Override
		public String toString(){
			return root.getPath();
		}
	}

}
