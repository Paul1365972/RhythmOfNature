package io.github.paul1365972.rhythmofnature.client.managers;

class PreTexture {
	
	private int id;
	private String name;
	private String totalPath;
	
	public PreTexture(int id, String name, String totalPath) {
		this.id = id;
		this.name = name;
		this.totalPath = totalPath;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTotalPath() {
		return totalPath;
	}
}
