package she.enumeration;

public enum TypeFeature {

	DOWNLOAD(1, "Download"), INSTALL(2, "Install"), UNISTALL(3, "Unistall");

	private Integer id;
	private String description;

	private TypeFeature(Integer id, String description) {
		this.setId(id);
		this.setDescription(description);
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
