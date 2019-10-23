package eubr.atmosphere.tma.analyze.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.utils.DatabaseManager;
import eubr.atmosphere.tma.utils.PerformanceScore;
import eubr.atmosphere.tma.utils.ResourceConsumptionScore;
import eubr.atmosphere.tma.utils.SecurityScore;
import eubr.atmosphere.tma.utils.TrustworthinessScore;
import eubr.atmosphere.tma.analyze.utils.Constants;
import eubr.atmosphere.tma.analyze.utils.PropertiesManager;

public class DataManager {

	private Connection connection = DatabaseManager.getConnectionInstance();

	private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class);
	public final List<Integer> monitoredPods = new ArrayList<Integer>();
	public final Integer probeIdResourceConsumption;
	public final Integer probeIdPerformance;
	public final Integer probeIdSecurity;

	public DataManager(String monitoredPodsString) {
		this.connection = DatabaseManager.getConnectionInstance();
		this.probeIdResourceConsumption = Integer
				.parseInt(PropertiesManager.getInstance().getProperty("probeIdResourceConsumption"));
		this.probeIdPerformance = Integer.parseInt(PropertiesManager.getInstance().getProperty("probeIdPerformance"));
		this.probeIdSecurity = Integer.parseInt(PropertiesManager.getInstance().getProperty("probeIdSecurity"));

		String[] pods = monitoredPodsString.split(",");
		for (int i = 0; i < pods.length; i++)
			this.monitoredPods.add(Integer.parseInt(pods[i]));
	}

	public ResourceConsumptionScore getDataResourceConsumption(String stringTime, int resource) {
		String sql = "select descriptionId, resourceId, avg(value) as value from Data " + "where "
				+ "DATE_FORMAT(valueTime, \"%Y-%m-%d %H:%i:%s\") >= ? AND" + "(probeId = ?) "
				+ "group by descriptionId, resourceId;";
		if (this.connection != null) {
			return executeQueryResourceConsumption(stringTime, sql, resource);
		} else {
			LOGGER.error("The connection is null!");
			return null;
		}
	}

	public PerformanceScore getDataPerformance(String stringTime, int resource) {
		String sql = "select descriptionId, resourceId, avg(value) as value from Data " + "where "
				+ "DATE_FORMAT(valueTime, \"%Y-%m-%d %H:%i:%s\") >= ? AND" + "(probeId = ?) "
				+ "group by descriptionId, resourceId;";
		if (this.connection != null) {
			return executeQueryPerformance(stringTime, sql, resource);
		} else {
			LOGGER.error("The connection is null!");
			return null;
		}
	}

	public SecurityScore getDataSecurity(String stringTime) {

		String sql = "select descriptionId, resourceId, value from Data " + "where "
				+ "DATE_FORMAT(valueTime, \"%Y-%m-%d %H:%i:%s\") >= ? AND" + "(probeId = ?) "
				+ "group by descriptionId, resourceId;";
		if (this.connection != null) {
			return executeQuerySecurity(stringTime, sql);
		} else {
			LOGGER.error("The connection is null!");
			return null;
		}

	}

	private SecurityScore executeQuerySecurity(String stringTime, String sql) {
		SecurityScore score = new SecurityScore();
		score.setMetricId(Constants.securityDellMetricId);
		score.setValueTime(System.currentTimeMillis() / 1000);

		try {
			PreparedStatement ps = this.connection.prepareStatement(sql);
			ps.setString(1, stringTime);
			ps.setInt(2, probeIdSecurity);
			ResultSet rs = DatabaseManager.executeQuery(ps);

			if (rs.next()) {
				do {
					int descriptionId = ((Integer) rs.getObject("descriptionId"));
					int resourceId = ((Integer) rs.getObject("resourceId"));
					Double value = ((Double) rs.getObject("value"));

					switch (descriptionId) {

					case Constants.ExistenceOfBestPracticeDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setExistenceOfBestPractice(resourceId,value);
						} else {
							LOGGER.debug("Something is not right! " + stringTime);
						}
						break;

					case Constants.ExistenceOfCheckAreaDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setExistenceOfCheckAreas(resourceId,value);
						} else {
							LOGGER.debug("Something is not right! " + stringTime);
						}
						break;

					case Constants.ExistenceOfPolicyDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setExistenceOfPolicy(resourceId,value);
						} else {
							LOGGER.debug("Something is not right! " + stringTime);
						}
						break;

					case Constants.ExistenceOfSecurityControlDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setExistenceOfSecurityControl(resourceId,value);
						} else {
							LOGGER.debug("Something is not right! " + stringTime);
						}
						break;
					case Constants.ExistenceOfSecurityDefinitionsDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setExistenceOfSecuritySefnition(resourceId,value);
						} else {
							LOGGER.debug("Something is not right! " + stringTime);
						}
						break;

					default:
						LOGGER.debug("Something is not right! {}, descriptionId: {}", stringTime, descriptionId);
						break;
					}
					// String valueTime = rs.getObject("valueTime").toString();
				} while (rs.next());

			} else {
				LOGGER.info("No data on: " + stringTime);
			}
			return score;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return score;
	}


	private ResourceConsumptionScore executeQueryResourceConsumption(String stringTime, String sql, int resource) {
		ResourceConsumptionScore score = null;
		score.setResourceId(resource);
                score.setMetricId(Constants.resourceConsumptionMetricId);
		try {
			PreparedStatement ps = this.connection.prepareStatement(sql);
			ps.setString(1, stringTime);
			ps.setInt(2, probeIdResourceConsumption);
			ResultSet rs = DatabaseManager.executeQuery(ps);

			if (rs.next()) {
				score = new ResourceConsumptionScore();
				int cpuCount = 0;
				int memoryCount = 0;
				do {
					int descriptionId = ((Integer) rs.getObject("descriptionId"));
					int resourceId = ((Integer) rs.getObject("resourceId"));
					Double value = ((Double) rs.getObject("value"));

					switch (descriptionId) {

					case Constants.cpuDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setCpuPod(score.getCpuPod() + value);
						} else {
							if (resourceId == Constants.nodeId) {
								// score.setCpuNode(value);
								// However, it was decided to change the score to use the maximum of the CPU
								// capacity.
								// This way, we will know how much CPU the pod is using
								score.setCpuNode(Constants.maxCPU);
							} else {
								LOGGER.debug("Something is not right! " + stringTime);
							}
						}
						break;

					case Constants.memoryDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							// It is needed to convert from bytes to Mi
							score.setMemoryPod(score.getMemoryPod() + value / 1024);
						} else {
							if (resourceId == Constants.nodeId) {
								// It is necessary to divide per 1024 to convert from bytes to Mi.
								// score.setMemoryNode(value / 1024);
								// However, it was decided to change the score to use the maximum of the memory
								// capacity.
								// This way, we will know how much Memory the pod is using
								score.setMemoryNode(Constants.maxMemory);
							} else {
								LOGGER.debug("Something is not right! " + stringTime);
							}
						}
						break;

					default:
						LOGGER.debug("Something is not right! {}, descriptionId: {}", stringTime, descriptionId);
						break;
					}
					// String valueTime = rs.getObject("valueTime").toString();
				} while (rs.next());

				LOGGER.debug("cpuCount: {} / memoryCount: {}", cpuCount, memoryCount);
			} else {
				LOGGER.info("No data on: " + stringTime);
			}
			return score;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return score;
	}

	private PerformanceScore executeQueryPerformance(String stringTime, String sql, int resource) {
		PerformanceScore score = new PerformanceScore();
		score.setMetricId(Constants.performanceMetricId);
		score.setResourceId(resource);
		try {
			PreparedStatement ps = this.connection.prepareStatement(sql);
			ps.setString(1, stringTime);
			ps.setInt(2, probeIdPerformance);
			ResultSet rs = DatabaseManager.executeQuery(ps);

			if (rs.next()) {
				do {
					int descriptionId = ((Integer) rs.getObject("descriptionId"));
					int resourceId = ((Integer) rs.getObject("resourceId"));
					Double value = ((Double) rs.getObject("value"));
					score.setResourceId(resourceId);
					score.setMetricId(Constants.performanceMetricId);

					switch (descriptionId) {

					case Constants.responseTimeDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setResponseTime(value);
						} else {
							LOGGER.debug("Something is not right! " + stringTime);
						}
						break;

					case Constants.throughputDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setThroughput(value);
						} else {
							LOGGER.debug("Something is not right! " + stringTime);
						}
						break;

					case Constants.demandDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setDemand(value);
						} else {
							LOGGER.debug("Something is not right! " + stringTime);
						}
						break;

					case Constants.rateRequestUnderContractedDescriptionId:
						if (isMonitorizedResource(resourceId)) {
							score.setRateRequestUnderContracted(value);
						} else {
							LOGGER.debug("Something is not right! " + stringTime);
						}
						break;

					default:
						LOGGER.debug("Something is not right! {}, descriptionId: {}", stringTime, descriptionId);
						break;
					}
					// String valueTime = rs.getObject("valueTime").toString();
				} while (rs.next());

			} else {
				LOGGER.info("No data on: " + stringTime);
			}
			return score;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return score;
	}

	public boolean isMonitorizedResource(int podId) {
		return this.monitoredPods.contains(podId);
	}

	public List<Double> getValuesPeriod(String initialDateTime, String finalDateTime, int descriptionId,
			int resourceId) {
		String sql = "select * from Data " + "where " + "valueTime between ? and ? and " + "descriptionId = ? and "
				+ "resourceId = ? " + "order by valueTime;";

		List<Double> values = new ArrayList<Double>();

		try {
			PreparedStatement ps = this.connection.prepareStatement(sql);
			ps.setString(1, initialDateTime);
			ps.setString(2, finalDateTime);
			ps.setInt(3, descriptionId);
			ps.setInt(4, resourceId);

			ResultSet rs = DatabaseManager.executeQuery(ps);

			while (rs.next()) {
				Double value = ((Double) rs.getObject("value"));
				values.add(value);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return values;
	}

	public int[] saveScore(TrustworthinessScore score) {
		// TODO This method should save not only the Trustworthiness score, but also the 
		//      resourceConsumptionScore and performanceScore
		
		// TODO Use the following constants:
		//Constants.resourceConsumptionMetricId;
		//Constants.performanceMetricId;
		//Constants.trustworthinessMetricId;
		
		// The score will be saved in the MetricData table
		
		
        String sql =
                "INSERT INTO MetricData(metricId, valueTime, value, resourceId) "
                + "VALUES (?, FROM_UNIXTIME(?), ?, ?)";
        PreparedStatement ps;

        try {
            ps = DatabaseManager.getConnectionInstance().prepareStatement(sql);
            
            ps.setInt(1, score.getMetricId());
            ps.setLong(2, score.getValueTime());
            ps.setDouble(3, score.getScore());
            ps.setInt(4, score.getResourceId());
            ps.addBatch();
            
            ResourceConsumptionScore rcScore = score.getResourceConsumptionScore();
            ps.setInt(1, rcScore.getMetricId());
            ps.setLong(2, rcScore.getValueTime());
            ps.setDouble(3, rcScore.getScore());
            ps.setInt(4, rcScore.getResourceId());
            ps.addBatch();
            
            PerformanceScore performanceScore = score.getPerformanceScore();
            ps.setInt(1, performanceScore.getMetricId());
            ps.setLong(2, performanceScore.getValueTime());
            ps.setDouble(3, performanceScore.getScore());
            ps.setInt(4, performanceScore.getResourceId());
            ps.addBatch();

            DatabaseManager databaseManager = new DatabaseManager();
            return databaseManager.executeBatch(ps);
        } catch (SQLException e) {
            LOGGER.error("[ATMOSPHERE] Error when inserting a plan in the database.", e);
        }
        return null;
	}
}
