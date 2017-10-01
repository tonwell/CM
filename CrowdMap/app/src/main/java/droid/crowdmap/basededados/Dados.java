package droid.crowdmap.basededados;

public class Dados {
    private long id;
	private double sinal, latitude, longitude;
	private String operadora, data;

    public Dados() { }

	public Dados(double sinal, double latitude, double longitude,
			String operadora) {
		this.sinal = sinal;
		this.latitude = latitude;
		this.longitude = longitude;
		this.operadora = operadora;
	}

	public Dados(double sinal, double latitude, double longitude,
			String operadora, String data) {

		this.sinal = sinal;
		this.latitude = latitude;
		this.longitude = longitude;
		this.operadora = operadora;
		this.data = data;
	}

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

	public double getSinal() {
		return sinal;
	}

	public void setSinal(double sinal) {
		this.sinal = sinal;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getOperadora() {
		return operadora;
	}

	public void setOperadora(String operadora) {
		this.operadora = operadora;
	}

	public boolean isOK() {
		if (sinal != 0 && longitude != 0 && latitude != 0) {
			return true;
		} else
			return false;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

    public String toString() {
        return operadora + " " + sinal + " " + latitude + " " + " " + longitude
                + " ";
    }
}