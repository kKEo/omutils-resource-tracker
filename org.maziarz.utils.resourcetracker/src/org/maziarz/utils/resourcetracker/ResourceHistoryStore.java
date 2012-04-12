package org.maziarz.utils.resourcetracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.h2.jdbcx.JdbcConnectionPool;
import org.maziarz.utils.resourcetracker.views.TagCloudItem;

public class ResourceHistoryStore {

	private static final String DBNAME = "file-history-store";
	private static IPath dbBasePath;

	private JdbcConnectionPool pool;

	public ResourceHistoryStore(IPath dbLocation) {

		this.dbBasePath = dbLocation;

		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			Activator.error(e.getMessage(), e);
		}

		String connectionString = getConnectionString();

		Activator.info("Jdbc: " + connectionString);
		pool = JdbcConnectionPool.create(connectionString, "", "");

		initializeDbIfNeeded(pool);
	}

	public void dispose() {
		pool.dispose();
	}

	public Connection getConnection() throws SQLException {
		return pool.getConnection();
	}

	private void initializeDbIfNeeded(JdbcConnectionPool pool) {

		try {
			Connection connection = getConnection();
			PreparedStatement ps = connection
					.prepareStatement("select 1 from hist");
			ps.executeQuery();
			connection.close();
			return;
		} catch (SQLException e1) {
			// do nothing
		}

		InputStream is = ResourceHistoryStore.class
				.getResourceAsStream("initDb.sql");

		BufferedReader bufReader = new BufferedReader(new InputStreamReader(is));

		try {
			char[] buffer = new char[1024];

			Writer writer = new StringWriter();
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}

			Connection connection = getConnection();
			Statement statement = connection.createStatement();
			try {
				String initScript = writer.toString();

				statement.executeUpdate(initScript);
			} finally {
				statement.close();
				connection.close();
			}
		} catch (IOException e) {
			Activator.error("Read schema.sql", e);
		} catch (SQLException e) {
			Activator.error(e.getMessage(), e);
		} finally {
			try {
				bufReader.close();
			} catch (IOException e) {
				Activator.error(e.getMessage(), e);
			}
		}
	}

	private String getConnectionString() {
		String path = "mem:db1";
		if (dbBasePath != null) {
			path = dbBasePath.append(DBNAME).toOSString()
					+ ";UNDO_LOG=0;LOCK_MODE=0";
		}
		return "jdbc:h2:" + path;
	}

	public void addChange(IFile path) {
		this.addChange(path, path.getLocalTimeStamp());
	}

	public void addChange(IFile path, long mDate) {

		Timestamp timestamp = new Timestamp(mDate);

		PreparedStatement ps;
		Connection connection = null;
		try {
			connection = getConnection();
			ps = connection
					.prepareStatement("insert into hist values (?,?,?,?)");
			ps.setString(1, path.getProjectRelativePath().toOSString());
			ps.setString(2, path.getName());
			ps.setString(3, path.getProject().getName());
			ps.setTimestamp(4, timestamp);
			ps.executeUpdate();

		} catch (SQLException e) {
			Activator.error(e.getMessage(), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// do nothing
				}
			}
		}

	}

	public List<TagCloudItem> getLastModified(String pattern, String project,
			int limit) {

		List<TagCloudItem> result = new ArrayList<TagCloudItem>(10);

		String where = "";
		if (pattern != null && !"".equals(pattern)) {
			where = " where name like '"
					+ pattern.replace('*', '%').replace("'", "?") + "'";
		}

		if (project != null && !"".equals(project)) {
			where = " where project = '" + project + "'";
			if (pattern != null && !"".equals(pattern)) {
				where = " and name like '"
						+ pattern.replace('*', '%').replace("'", "?") + "'";
			} else {
			}
		}

		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn
					.prepareStatement("select path, project, mdate, c from ("
							+ "select path, project, count(1) c, max(mdate) mdate from Hist"
							+ where + " group by path, project)"
							+ " order by mdate desc");
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				TagCloudItem item = new TagCloudItem(rs.getString(1),
						rs.getString(2), rs.getString(3));
				result.add(item);
			}
			rs.close();

		} catch (SQLException e) {
			Activator.error(e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// do nothing
				}
			}
		}

		return result;

	}
	
	public String getTags(IResource file) {
		PreparedStatement ps;
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			ps = connection
					.prepareStatement("select tags from tags where path = ?");
			ps.setString(1, file.getFullPath().toOSString());
			
			rs = ps.executeQuery();

			while (rs.next()) {
				return rs.getString(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				connection.close();
			} catch (SQLException e) {
				// ignore this
			}
		}
		return "";
	}

	public void updateTags(IResource s, String tags) {
		
		String currentTags = getTags(s);
		
		PreparedStatement ps;
		Connection connection = null;
		
		try {
			connection = getConnection();
			if (tags != null && !currentTags.equals("")) {
				ps = connection
						.prepareStatement("update tags set tags = ? where path = ?");
				
				ps.setString(1, tags);
				ps.setString(2, s.getFullPath().toOSString());
				
			} else {
				ps = connection
						.prepareStatement("insert into tags values (?,?,?)");
				
				ps.setNull(1, Types.VARCHAR);
				ps.setString(2, s.getFullPath().toOSString());
				ps.setString(3, tags);
			}

			ps.executeUpdate();

//			updateStats(newTags, currentTags);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				// just ignore
			}
		}
	}

	private void updateStats(String newTags, String currentTags) {
		String[] aCurrentTags = currentTags.split(",");
		String[] aNewTags = newTags.split(",");
		
		loop:for (String tag : aNewTags) {
			if (!"".equals(tag)) {
				for (String ctag : aCurrentTags) {
					if (tag.equalsIgnoreCase(ctag)) {
						continue loop;
					}
				}
				
			}
		}
	}


}
