package plugins;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.LabelValueBean;

public class DatabasePlugin implements PlugIn {
	public DatabasePlugin() {
	}

	static ServletContext context = null;
	public static DataSource datasource = null;

	public void init(ActionServlet actionServlet, ModuleConfig config) {
		context = actionServlet.getServletContext();
		datasource = (DataSource) context.getAttribute("dbsource");

		System.out.println("Datebase plugin  Initalized for AP-OLCMS context.............................................."+datasource);

	}


	public static Connection connect() {
		Connection con = null;
		try {
			if (con == null) {
				con = datasource.getConnection();
				System.out.println("-->: APOLCMS-SERVICES AFTER CONNECTING : Active : " + ((BasicDataSource)datasource).getNumActive() + " - Idle : " + ((BasicDataSource)datasource).getNumIdle() );
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return con;
	}

	public static int executeUpdatePOA(String statement, byte[] obj1,
			byte[] obj2, byte[] obj3, byte[] obj4) throws SQLException {
		PreparedStatement ps = null;
		int updateCount = 0;
		Connection con = null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(statement);
			ps.setBytes(1, obj1);
			ps.setBytes(2, obj2);
			ps.setBytes(3, obj3);
			ps.setBytes(4, obj4);
			updateCount = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, null, null);
			closePst(ps);
		}
		return updateCount;
	}

	public static String getStringfromQuery(String sql, Connection con) {
		//System.out.println("sql::" + sql);
		String first = "";
		Statement st=null;
		ResultSet rs=null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				first = rs.getString(1);
			}else
				first="0";
		} catch (SQLException se) {
			first = "0";
			se.printStackTrace();
		} finally {
			close(null, st, rs);
		}
		return first;
	}


	public static String getStringfromQuery(Connection conn, String query, Object objArray) throws SQLException
	{
		String str = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(query);
			//System.out.println("query :: "+query);
			if(objArray != null){
				ps.setObject(1, objArray);
			}
			rs= ps.executeQuery();
			if (rs.next()) {
				str = rs.getString(1);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		finally {
			close(null, null, rs);
			closePst(ps);
		}
		return str;
	}

	public static int executeUpdate(String sql,Connection con) {
		int a = 0;
		Statement st=null;
		ResultSet rs=null;
		try {
			st = con.createStatement();
			a = st.executeUpdate(sql);
		} catch (SQLException se) {
			a = 0;
			se.printStackTrace();
		} finally {
			close(null, st, rs);
		}
		return a;
	}

	public static ArrayList selectQuery(DataSource ds, String sql) {
		ArrayList a = new ArrayList();
		Connection con=null;
		Statement st=null;
		ResultSet rs=null;
		try {
			con = ds.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(sql);
			a = getRs2ArrayList(rs);
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			close(con, st, rs);
		}
		return a;
	}

	public static ArrayList selectQuery(Connection con, String sql) {
		ArrayList a = new ArrayList();
		Statement st=null;
		ResultSet rs=null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(sql);
			a = getRs2ArrayList(rs);
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			close(null, st, rs);
		}
		return a;
	}


	public static List executeQuery(Connection conn, String query, Object[] objArray)
			throws SQLException
			{
		PreparedStatement ps = conn.prepareStatement(query);
		//System.out.println(" qry >> "+query);
		for (int i = 0; i < objArray.length; i++) {
			ps.setObject(i + 1, objArray[i]);
			//System.out.println(" obj >> "+objArray[i]);
		}
		ResultSet rs = ps.executeQuery();
		List result = processResultSet(rs);
		close(null, ps, rs);
		return result;
			}

	/*
	 * public static ArrayList selectQuery4singlerow(String sql) { ArrayList a =
	 * new ArrayList(); try { con = datasource.getConnection(); st =
	 * con.createStatement(); rs = st.executeQuery(sql); int colCount =
	 * getColCount(rs); int i;
	 * 
	 * 
	 * for ( i = 1; i <= colCount; i++ ); // {
	 * 
	 * a.add(rs.getString(i));i++;
	 * 
	 * 
	 * }
	 * 
	 * 
	 * 
	 * // for ( i = 1; i <= colCount; rs.next() ) // { // // continue; // // //
	 * // a.add(rs.getString(i));i++; // //rs.next(); // }
	 * 
	 * while (rs.next()) { a.add(rs.getString(i)); String un =
	 * rs.getString("name"); //finalSearch = finalSearch + un + "\n"; } } catch
	 * (SQLException se) { se.printStackTrace(); } finally { close(con, st, rs);
	 * 
	 * } return a;
	 * 
	 * }
	 */
	public static ArrayList getRs2ArrayList(ResultSet rs) {
		ArrayList matrix = new ArrayList();
		ArrayList row = null;
		try {
			if (rs != null) {
				int colCount = getColCount(rs);
				while (rs.next()) {
					row = new ArrayList();
					for (int i = 1; i <= colCount; i++) {
						row.add(rs.getString(i));
					}
					matrix.add(row);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return matrix;
	}

	public static int getColCount(ResultSet rs) {
		int rowCount = 0;
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			rowCount = rsmd.getColumnCount();
		} catch (Exception localException) {
		}

		return rowCount;
	}

	public static ArrayList getLabelValueBeans(ArrayList twoDimList,
			boolean forCombo) {
		ArrayList lvBeans = new ArrayList();
		if ((twoDimList != null) && (twoDimList.size() != 0)
				&& (((ArrayList) twoDimList.get(0)).size() <= 2)
				&& (((ArrayList) twoDimList.get(0)).size() >= 1)) {
			Iterator iter = twoDimList.iterator();
			while (iter.hasNext()) {
				ArrayList rowItem = (ArrayList) iter.next();

				if (rowItem.size() > 1) {
					lvBeans.add(new LabelValueBean((String) rowItem.get(0),
							(String) rowItem.get(1)));
				} else {
					lvBeans.add(new LabelValueBean((String) rowItem.get(0),
							(String) rowItem.get(0)));
				}
			}
		}
		if (forCombo) {
			lvBeans.add(0, new LabelValueBean("--Select--", "0"));
		}
		return lvBeans;
	}

	public static void closeConnection(Connection con) {
		try {
			if (con != null) {
				con.close();
			}
			System.out.println("-->: APOLCMS AFTER CLOSING : Active : " + ((BasicDataSource)datasource).getNumActive() + " - Idle : " + ((BasicDataSource)datasource).getNumIdle() );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void closeConnection(Connection con,PreparedStatement ps,ResultSet rs,Statement st ) {
		try {
			if(ps != null)
				ps.close();
			if(rs != null)
				rs.close();
			if(st != null)
				st.close();
			if (con != null) {
				closeConnection(con);
			}
			System.out.println("-->: APOLCMS AFTER CLOSING : Active : " + ((BasicDataSource)datasource).getNumActive() + " - Idle : " + ((BasicDataSource)datasource).getNumIdle() );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void close(Connection con, Statement st, ResultSet rs) {
		try {


			if (rs != null) {
				rs.close();
			}

			if (st != null) {
				st.close();
			}

			if (con != null) {
				closeConnection(con);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	public static int executeBatch(ArrayList sqls) throws SQLException {
		int value = 0;
		Connection con=null;
		Statement st=null;
		ResultSet rs=null;
		try {
			con = datasource.getConnection();
			con.setAutoCommit(false);
			if ((con == null) || (sqls == null) || (sqls.size() == 0))
				throw new SQLException("executeBatch(): NULL Connection/Query ");
			st = con.createStatement();
			for (int i = 0; i < sqls.size(); i++) {

				st.addBatch((String) sqls.get(i));
			}
			int[] count = st.executeBatch();

			System.out.println("the execute batch Count is --------- "+count.length);

			/*for (int i = 0; i < count.length; i++)
			{
				if (value > count[i]){ 
					value = count[i];
					}
				System.out.println("the execute batch Count is --------- "+count[i]);
				System.out.println("the execute batch Count is --------- "+value);
			}*/

			if(count!=null)
			{
				value=1;	

			}

			con.commit();
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			close(con, st, rs);
		}
		System.out.println("the execute batch Count is --------- "+value);
		return value;
	}





	public static int executeBatchSQLs(ArrayList sqls, Connection con)
			throws Exception {
		int value = 0;
		Statement st=null;
		try {
			if ((con != null) && (sqls != null) && (sqls.size() > 0)) {
				st = con.createStatement();
				for (int i = 0; i < sqls.size(); i++) {
					st.addBatch((String) sqls.get(i));
				}
				int[] count = st.executeBatch();
				if (count != null) {
					value = 1;
				}
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			close(null, st, null);
		}
		//System.out.println("the execute batch Count is --------- " + value);
		return value;
	}


	public static String selectQuerySA(String sql) {
		String finalSearch = "";
		Connection con=null;
		Statement st=null;
		ResultSet rs=null;
		try {
			con = datasource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String un = rs.getString("name");
				finalSearch = finalSearch + un + "\n";
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			close(con, st, rs);
		}
		return finalSearch;
	}

	public static List executeQuery(String sql) throws SQLException {
		List result = null;
		Connection con=null;
		PreparedStatement ps = null;
		ResultSet rs=null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			result = processResultSet(rs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, null, rs);
			closePst(ps);
		}

		return result;
	}

	public static List executeQuery(String sql,Connection con) throws SQLException {
		List result = null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			result = processResultSet(rs);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			close(null, null, rs);
			closePst(ps);
		}
		return result;
	}


	public static List processResultSet(ResultSet rs) throws SQLException {
		List result = null;
		ResultSetMetaData rm = rs.getMetaData();
		int cols = rm.getColumnCount();
		if (rs.next()) {
			result = new ArrayList();
			do {
				Map row = new HashMap(cols);
				for (int i = 1; i <= cols; i++) {
					String columnName = rm.getColumnName(i);
					Object columnValue = rs.getObject(i);
					if ((columnValue == null) || (columnValue.equals(""))) {
						columnValue = " ";
					}
					row.put(columnName, columnValue);
				}

				result.add(row);
			} while (

					rs.next());
		}
		return result;
	}

	public static List getBytesInList(String sql) throws SQLException {
		List result = null;
		Connection con=null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			result = processResultSet1(rs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, null, rs);
			closePst(ps);
		}

		return result;
	}

	private static List processResultSet1(ResultSet rs) throws SQLException {
		String txt = "";
		List result = null;
		ResultSetMetaData rm = rs.getMetaData();
		int cols = rm.getColumnCount();
		if (rs.next()) {
			result = new ArrayList();
			do {
				Map row = new HashMap(cols);
				for (int i = 1; i <= cols; i++) {
					String columnName = rm.getColumnName(i);
					byte[] columnValue = rs.getBytes(i);

					if ((columnValue == null) || (columnValue.equals(""))) {
						txt = "";
					} else {
						txt = new String(columnValue);
					}

					row.put(columnName, txt);
				}

				result.add(row);
			} while (

					rs.next());
		}
		return result;
	}

	public static ResultSet executeQueryResultSet(String sql)
			throws SQLException {
		Connection con=null;
		Statement st=null;
		ResultSet rs=null;
		try {
			con = datasource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(sql);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (con != null)
					con.close();
			} catch (Exception localException1) {
			}
		} finally {
			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception localException2) {
			}
		}

		return rs;
	}

	public static int executeUpdate(String statement, byte[] obj)
			throws SQLException {
		Connection con=null;
		PreparedStatement ps = null;
		int updateCount = 0;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(statement);
			ps.setBytes(1, obj);
			updateCount = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, null, null);
			closePst(ps);
		}
		return updateCount;
	}

	public static int executeUpdate1(String statement, ArrayList objArray)
			throws SQLException {
		Connection con=null;
		PreparedStatement ps = null;
		int updateCount = 0;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(statement);
			if ((objArray != null) && (objArray.size() != 0)) {
				for (int j = 0; j < objArray.size(); j++) {
					ArrayList subArray = (ArrayList) objArray.get(j);

					if ((subArray != null) && (subArray.size() != 0)) {
						for (int i = 0; i < subArray.size(); i++) {
							ps.setObject(i + 1, subArray.get(i));
						}
						updateCount += ps.executeUpdate();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, null, null);
			closePst(ps);
		}
		return updateCount;
	}

	public static void closePst(PreparedStatement pst) {
		try {
			if (pst != null) {
				pst.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] getBytesfromDB(String sql) {
		byte[] first = (byte[]) null;
		Connection con=null;
		Statement st=null;
		ResultSet rs=null;
		try {
			con = datasource.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				first = rs.getBytes(1);
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			close(con, st, rs);
		}

		return first;
	}

	public static ArrayList executeRadioQuery(String sql) throws SQLException {
		ArrayList result = null;
		Connection con=null;
		PreparedStatement ps=null;
		ResultSet rs=null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = new ArrayList();
				result.add(rs.getString(1));
				byte[] columnValue = rs.getBytes(2);
				result.add(new String(columnValue));
				result.add(rs.getString(3));
				result.add(rs.getString(4));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, null, rs);
			closePst(ps);
		}

		return result;
	}

	public static int executeUpdate(String statement, Object[][] objArray)
			throws SQLException {
		int updateCount = 0;
		Connection con=null;
		try {
			con = datasource.getConnection();
			updateCount = executeUpdate(con, statement, objArray);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, null, null);
		}
		return updateCount;
	}

	public static int executeUpdate(Connection conn, String statement,
			Object[][] objArray) throws SQLException {
		PreparedStatement ps =null;
		int updateCount = 0;
		try {
			ps = conn.prepareStatement(statement);

			for (int j = 0; j < objArray.length; j++) {
				for (int i = 0; i < objArray[j].length; i++) {
					ps.setObject(i + 1, objArray[j][i]);
				}
				updateCount += ps.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closePst(ps);
		}
		return updateCount;
	}

	public static int executeUpdate(Connection conn, String statement,
			Object[] objArray) throws SQLException {
		PreparedStatement ps =null;
		int updateCount = 0;
		try {
			ps = conn.prepareStatement(statement);
			for (int j = 0; j < objArray.length; j++) {
				/*System.out.println(objArray[j]);*/
				ps.setObject(j + 1, objArray[j]);
			}
			updateCount += ps.executeUpdate();
			//System.out.println(updateCount);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closePst(ps);
		}
		return updateCount;
	}

	public static Object executeScalar(String statement) throws SQLException {
		Object data = null;
		Connection con=null;
		PreparedStatement ps =null;
		ResultSet rs=null;
		try {
			con = datasource.getConnection();
			ps = con.prepareStatement(statement);
			rs=ps.executeQuery();
			if (rs.next())
				data = rs.getObject(1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(con, null, rs);
			closePst(ps);
		}
		return data;
	}

	public static ArrayList getArrayList(boolean isForCombo, String SQL,
			String errormsg) {
		ArrayList arrList = new ArrayList();
		ArrayList tempArrayList = new ArrayList();
		Connection conn = null;
		Statement st = null;
		ResultSet rs=null;
		try {
			conn = connect();
			st = conn.createStatement();
			rs = st.executeQuery(SQL);

			if (rs != null) {
				while (rs.next()) {
					if (isForCombo) {
						arrList.add(new LabelValueBean(rs.getString(2), rs
								.getString(1)));
					} else {
						tempArrayList = new ArrayList();
						tempArrayList.add(rs.getString(2));
						tempArrayList.add(rs.getString(1));
						arrList.add(tempArrayList);
					}

				}

			}

		} catch (SQLException sql) {
			sql.printStackTrace();

			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		} finally {
			try {
				if (st != null)
					st.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}

		return arrList;
	}

	public static ArrayList selectQuery(String sql) {
		ArrayList a = new ArrayList();
		Connection con=null;
		Statement st=null;
		ResultSet rs=null;
		try {
			con = connect();
			st = con.createStatement();
			rs = st.executeQuery(sql);
			a = getRs2ArrayList(rs);
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			close(con, st, rs);
		}
		return a;
	}

	public static ArrayList getDistricts(boolean isForCombo)
			throws SQLException {
		String SQL = "select DISTRICT_ID, district_name from district_mst order by DISTRICT_ID";
		return getArrayList(isForCombo, SQL,
				"Error at GeneralQueries.getDistricts");
	}

	public static ArrayList getAllGps(boolean isForCombo) throws SQLException {
		String SQL = "SELECT GP_NO,GP_NAME FROM GPOFFICE_MST";
		return getArrayList(isForCombo, SQL,
				"Error at GeneralQueries.getDistricts");
	}

	public static ArrayList getFeeTypes(boolean isForCombo) throws SQLException {
		String SQL = "SELECT SL_NO,FEE_TYPE_NAME FROM FEE_TYPE_MASTER";
		return getArrayList(isForCombo, SQL,
				"Error at GeneralQueries.getDistricts");
	}

	public static ArrayList getCourts(String deptFlag) throws SQLException {
		String SQL = "SELECT SNO,COURT_NAME FROM COURT_MST where DELETE_FLAG='F' order by DISPLAYORDER";
		if ((deptFlag != null) && ("false".equals(deptFlag)))
			SQL = "SELECT SNO,COURT_NAME FROM COURT_MST where DELETE_FLAG='F' and SNO!=1 and SNO!=30";
		if ((deptFlag != null) && ("30".equals(deptFlag)))
			SQL = "SELECT SNO,COURT_NAME FROM COURT_MST where DELETE_FLAG='F'  and SNO=30";
		if ((deptFlag != null) && ("1".equals(deptFlag))) {
			SQL = "SELECT SNO,COURT_NAME FROM COURT_MST where DELETE_FLAG='F'  and SNO=1";
		}

		return getArrayList(true, SQL, "Error at GeneralQueries.getDistricts");
	}

	public static ArrayList getSubcourtList(String courtId, String distId) {
		System.out.println("distId=" + distId);
		ArrayList values = new ArrayList();
		if ((courtId != null) && (!"".equals(courtId))) {
			String sql = "SELECT SUB_COURTNAME,SLNO FROM SUB_COURT_MST where  PARENT_COURTID="
					+ courtId
					+ " "
					+ ((distId != null) && (!distId.equals("")) ?

							" and " + distId
							+ " in (select * from table(SPLIT(DIST_ID))) "
							: (courtId != null)
							&& ((courtId.equals("2"))
									|| (courtId.equals("1")) || (courtId
											.equals("14"))) ? "" : "")
											+ " ";
			System.out.println("subcourts==" + sql);
			try {
				values = selectQuery(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return values;
	}

	public static ArrayList getCasetypes(String courtId, String typeCase)
			throws SQLException {
		String condition = "";
		if ((typeCase != null) && (typeCase.equals("all"))) {
			condition = "";
		} else {
			condition = "and  PARENT_CASE_TYPE='" + typeCase + "'";
		}

		String SQL = "SELECT case_short_name,case_full_name FROM CASE_TYPE_MASTER where COURT_ID="
				+ courtId
				+ " "
				+ condition
				+ " and delete_flag='F' order by case_full_name";
		return getArrayList(true, SQL, "Error at GeneralQueries.getCasetypes");
	}

	public static ArrayList getQuery_ArraylistParam_map(String sql, Connection conn) {
		Statement st = null;
		ResultSet rs = null;

		ArrayList values = new ArrayList();
		try {
			int slno = 0;
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int i = rsmd.getColumnCount();
			if (rs != null && rs.next()) {
				do {
					Map m = new HashMap();
					slno = slno + 1;
					m.put("slno", slno);
					for (int j = 1; j <= i; j++) {
						if (rs.getObject(j) == null) {
							m.put(rsmd.getColumnName(j), "-");
						} else {
							m.put(rsmd.getColumnName(j), rs.getString(j));
						}
					}
					values.add(m);
				} while (rs.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
		return values;
	}
	
	public static ArrayList getQuery_ArraylistParam_map(String sql) {
		Connection conn=null;
		Statement st=null;
		ResultSet rs=null;


		ArrayList values = new ArrayList();
		try {
			int slno = 0;
			conn=datasource.getConnection();
			st = conn.createStatement();
			rs = st.executeQuery(sql);			
			ResultSetMetaData rsmd = rs.getMetaData();			
			int i = rsmd.getColumnCount();
			if (rs != null && rs.next()) {
				do {
					Map m = new HashMap();
					slno = slno + 1;
					m.put("slno", slno);
					for (int j = 1; j <= i; j++) {

						if (rs.getObject(j) == null) {
							m.put(rsmd.getColumnName(j), "-");
						} else {
							m.put(rsmd.getColumnName(j), rs.getString(j));
						}

					}
					values.add(m);

				} while (rs.next());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}finally{

			try{

				if(conn!=null) {

					if(!conn.isClosed()) {
						conn.close();
					}

				}
			}
			catch (Exception e2) {
				e2.getMessage();
			}




		}

		return values;
	}
	public static String lvb(String sql,Connection con){
		StringBuffer val = new StringBuffer();
		ArrayList li=doubleArrayList(sql, con);
		val.append("<option value='0'>---SELECT---</option>");
		for(int i=0 ; i <li.size() ;i++){
			val.append("<option value='"+((ArrayList)li.get(i)).get(0)+"'>"+((ArrayList)li.get(i)).get(1)+"</option>");
		}
		return val.toString();
	}
	public static ArrayList<ArrayList<String>> doubleArrayList(String sql,Connection con) {
		ArrayList<ArrayList<String>> matrix = new ArrayList<ArrayList<String>>();
		ResultSet rs=null;
		Statement st=null;
		try {
			st=con.createStatement();
			rs=st.executeQuery(sql);
			int columns=columnCount(rs);
			ArrayList<String> subMatrix = null;

			while(rs.next()) {
				subMatrix = new ArrayList<String>();

				for (int i=1;i<=columns;i++) {
					subMatrix.add(rs.getString(i));
				}

				matrix.add(subMatrix);
			}
		} catch (Exception e) {
			System.out.println("Error while copying the double dimensional ArrayList");
			e.printStackTrace();
		}
		return matrix;
	}
	public static int columnCount(ResultSet rs) {

		int columns=0;
		try {
			ResultSetMetaData rm;

			if (rs!=null) {
				rm = rs.getMetaData();
				columns=rm.getColumnCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return columns;
	}
	public synchronized static ArrayList<HashMap<String, Object>> selectQueryMap(String sql, Connection con) {
		ArrayList<HashMap<String, Object>> matrix= null;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			matrix = selectQueryMap(rs);

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {

			try {
				if (pstmt!=null) {
					pstmt.close();
				}
				if (rs!=null) {
					rs.close();
				}
			} catch (Exception e) {
				System.out.println("Entered into stack trace ::: closing the connection ");
				e.printStackTrace();
			}
		}
		return matrix;
	}
	public synchronized static ArrayList<HashMap<String, Object>> selectQueryMap(ResultSet rs) {
		ArrayList<HashMap<String, Object>> matrix= new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> subMatrix=null;

		try {
			ResultSetMetaData rm = rs.getMetaData();
			int columns=columnCount(rs);
			while(rs.next()) {
				subMatrix = new HashMap<String, Object>();

				for (int i=1;i<=columns;i++) {
					subMatrix.put(rm.getColumnName(i), rs.getString(i));
				}
				matrix.add(subMatrix);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return matrix;
	}
	public static ArrayList<LabelValueBean> getLabelValueBean(Connection con,String sql,boolean combo) {
		ArrayList<LabelValueBean> matrix = new ArrayList<LabelValueBean>();

		PreparedStatement pstmt =null;
		ResultSet rs = null;
		try {
			/*System.out.println("lvb");*/
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();

			if (combo)
				matrix.add(new LabelValueBean("---SELECT---",""));

			while(rs.next()) {
				matrix.add(new LabelValueBean(rs.getString(2),rs.getString(1)));
			}
			/*System.out.println(matrix);*/

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs);
			closePst(pstmt);
		}
		return matrix;
	}

	public static List executeQuery(Connection conn, String query)
			throws SQLException {
		PreparedStatement ps =null;
		ResultSet rs = null;
		List result =null;
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			result = processResultSet(rs);
			//System.out.println("result::"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs);
			closePst(ps);
		}
		return result;
	}

	public static ArrayList<LabelValueBean> abledPercentage() {
		ArrayList<LabelValueBean> matrix = new ArrayList<LabelValueBean>();
		try {

			matrix.add(new LabelValueBean("---SELECT---","0"));
			for(int i=40;i<=100;i++) {
				matrix.add(new LabelValueBean( String.valueOf(i), String.valueOf(i)));
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
		return matrix;
	}

	public static String selectString(String sql,Connection con) {
		//System.out.println(sql);
		Statement st=null;
		ResultSet rs=null;
		String str="";
		try {
			st=con.createStatement();
			rs = st.executeQuery(sql);
			if(rs!=null && rs.next())
				str=rs.getString(1);

		} catch(SQLException se) {
			se.printStackTrace();
		} finally {
			try {
				st.close();
				rs.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return str;
	}

	public static int[] executeBatchs(ArrayList sqls, Connection conn)
	{
		Statement st=null;
		int[] count={0};
		try{

			st = conn.createStatement();
			for(int i=0; i<sqls.size(); i++)
			{
				//System.out.println("sqls::::"+sqls.get(i));
				st.addBatch((String)sqls.get(i));
			}
			count =st.executeBatch();

		}
		catch (Exception e) {
			e.printStackTrace();
		}finally {
			closeStatement(st);
		}
		return count;
	}

	public static ArrayList getYearFromBegining() {
		ArrayList<LabelValueBean> years=new ArrayList<LabelValueBean>();
		Calendar cal=Calendar.getInstance();

		for(int i=1950;i<=cal.get(Calendar.YEAR);i++)
		{
			years.add(new LabelValueBean(Integer.toString(i),Integer.toString(i)));
		}

		return years;
	}

	public static ArrayList<LabelValueBean> getEducationListBean(Connection con,String sql,boolean combo) {
		ArrayList<LabelValueBean> matrix = new ArrayList<LabelValueBean>();

		PreparedStatement pstmt =null;
		ResultSet rs = null;
		try {
			/*System.out.println("lvb");*/
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();

			if (combo)
				matrix.add(new LabelValueBean("---SELECT---","0"));
			matrix.add(new LabelValueBean("NURSERY","NURSERY"));
			matrix.add(new LabelValueBean("LKG","L.K.G"));
			matrix.add(new LabelValueBean("UKG","U.K.G"));
			matrix.add(new LabelValueBean("1st CLASS","1"));
			matrix.add(new LabelValueBean("2nd CLASS","2"));
			matrix.add(new LabelValueBean("3rd CLASS","3"));
			matrix.add(new LabelValueBean("4th CLASS","4"));
			matrix.add(new LabelValueBean("5th CLASS","5"));
			matrix.add(new LabelValueBean("6th CLASS","6"));
			matrix.add(new LabelValueBean("7th CLASS","7"));
			matrix.add(new LabelValueBean("8th CLASS","8"));
			matrix.add(new LabelValueBean("9th CLASS","9"));
			matrix.add(new LabelValueBean("10th CLASS","10"));


			while(rs.next()) {
				matrix.add(new LabelValueBean(rs.getString(2),rs.getString(1)));
			}
			/*System.out.println(matrix);*/

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			closeResultSet(rs);
			closePst(pstmt);
		}
		return matrix;
	}

	public static boolean executeInsertUpdateALLQueries(ArrayList queries_list, Connection con1)
	{
		Statement stmt = null;
		int count = -1;

		boolean flag = true;
		try
		{
			con1.setAutoCommit(false);
			stmt  = con1.createStatement();
			for(int i=0;i<queries_list.size();i++)
			{
				String SQL = (String)queries_list.get(i);
				//System.out.println(SQL);
				count = stmt.executeUpdate(SQL);
				if ( count==-1)
				{
					flag = false;
					con1.rollback() ;
					break;
				}


			}
			if(flag)
			{
				con1.commit();
			}
			else
			{
				con1.rollback();
			}
			//System.out.println(flag);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try {
				flag=false;
				if( null!= con1 && !con1.isClosed())
					con1.rollback(); 
			} catch(Exception ce) {}
		}
		finally
		{
			try {
				if( null!= con1 && !con1.isClosed())
					con1.setAutoCommit(true);
			}catch(Exception ae) {}
		}
		return flag;
	}
	public static void closeResultSet(ResultSet rs) {
		try {
			if(rs != null){  
				rs.close();
			}
		}catch(SQLException se) {
			System.out.println(se.getMessage());
			se.printStackTrace();
		}
	}
	public static void closePreparedStatement(PreparedStatement pst) throws Exception {
		try{
			if (pst!=null){
				pst.close();  
			}
		}catch(SQLException se){
			throw new Exception("SQL Exception while closing "
					+"Prepared Stadatetement : \n" +se);
		}
	}
	public static void closeStatement(Statement pst) {
		try{
			if (pst!=null){
				pst.close();  
			}
		}catch(SQLException se){
			System.out.println(se.getMessage());
			se.printStackTrace();
		}
	}
	public static String getSingleValue(Connection conn, String query)
			throws SQLException {
		//System.out.println(query);
		PreparedStatement ps = null;
		ResultSet rs = null;
		String result = "";
		try{
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();

			if (rs!=null && rs.next()){
				result = rs.getString(1);
			}else
				result = "";
		}catch(SQLException se){
			System.out.println(se.getMessage());
			se.printStackTrace();
		}finally
		{
			closeResultSet(rs);
			closePst(ps);
		}
		return result;
	}
	public static ArrayList getSelectBox(String sql,Connection con)
	{
		ArrayList selectData = new ArrayList();
		Statement st =null;
		ResultSet rs = null;
		try
		{
			st=con.createStatement();
			rs=st.executeQuery(sql);
			while(rs!=null && rs.next())
			{
				selectData.add(new LabelValueBean(rs.getString(2),rs.getString(1)));
			}
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}
		finally
		{
			try
			{	
				if(st!=null)
					st.close();
				if(rs!=null)
					rs.close();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return selectData;
	}
	public synchronized static String getStringValue(String sql, Connection con) throws Exception{
		String value = "";
		Statement st=null;
		ResultSet rs=null;
		try{
			st = con.createStatement();
			System.out.println("sql"+sql);
			rs = st.executeQuery(sql);
			if(rs != null) {
				while(rs.next()) {
					value = rs.getString(1);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			closeResultSet(rs);
			closeStatement(st);
		}
		return value;
	}
	
	public static void setDefaultParameters(PreparedStatement ps,int i,Object val,String type) {
		try {
			if(type.equals("String")) {
				if(val != null && !val.toString().trim().equals("")) {
					ps.setString(i, val.toString());
				}else {
					ps.setNull(i, java.sql.Types.VARCHAR);
				}
			}else if(type.equals("Int")) {
				if(val != null && !val.toString().trim().equals("")) {
					ps.setInt(i, Integer.parseInt(val.toString()));
				}else {
					ps.setNull(i, java.sql.Types.INTEGER);
				}
			}else if(type.equals("Double")) {
				if(val != null && !val.toString().trim().equals("")) {
					ps.setDouble(i, Double.parseDouble(val.toString()));
				}else {
					ps.setNull(i, java.sql.Types.NUMERIC);
				}
			}else if(type.equals("Boolean")) {
				if(val != null && !val.toString().trim().equals("")) {
					ps.setBoolean(i, Boolean.parseBoolean(val.toString()));
				}else {
					ps.setNull(i, java.sql.Types.BOOLEAN);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean NullValidation(String str) {
		boolean flag = true; 
		
		if(str == null || str.equals("") || str.trim() == null || str.trim().equals("") || str.trim().equals("null"))
			flag = false;
		
		return flag;
	}
	
	public static boolean NonZeroValidation(String str) {
		boolean flag = true; 
		
		if(str == null || str.equals("") || str.trim() == null || str.trim().equals("") || str.trim().equals("null") || str.trim().equals("0"))
			flag = false;
		
		return flag;
	}
	/*	public static int[] executeBatch(ArrayList sqls, Connection conn)
	{
		int[] count={0};
		try{
		Statement st;
		st = conn.createStatement();
		for(int i=0; i<sqls.size(); i++)
		{
			System.out.println("sqls::::"+sqls.get(i));
			st.addBatch((String)sqls.get(i));
		}
		count =st.executeBatch();

		}
		catch (Exception e) {
					e.printStackTrace();
		}
		return count;
	}*/
	
	public static String randomTransactionNo() {
		String randomNo = null;
		Date d1 = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyHHmmss");
		randomNo = "" + sdf.format(d1);
		return randomNo;
	}
	
}
