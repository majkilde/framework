package majkilde.fw;


import lotus.domino.Session;
import lotus.domino.Database;
import lotus.domino.View;
import lotus.domino.Document;
import lotus.domino.NotesException; //needed
import com.debug.*; // XPages Debug toolbar
import com.ibm.domino.xsp.module.nsf.NotesContext; // Get currentDatabase & currentSession
import java.io.Serializable; // Make the bean serializable
import java.io.UnsupportedEncodingException; //URLEncoder
import java.net.URLEncoder; //URLEncoder
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

public class DbConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	//private properties & variables
	private Database configDb = null;
	private Database currentDb = null;
	private Session session = null;
	private DebugToolbar debug = null;

	// Constructor
	public DbConfig() {
		session = getCurrentSession();
		currentDb = getCurrentDatabase();
		debug = new DebugToolbar(); //initialize the debug toolbar
	}

	// -------------------------------------------------------------------------
	private Session getCurrentSession() {
		NotesContext nc = NotesContext.getCurrentUnchecked();
		return (null != nc) ? nc.getCurrentSession() : null;
	}

	// -------------------------------------------------------------------------
	private Database getCurrentDatabase() {
		NotesContext nc = NotesContext.getCurrentUnchecked();
		return (null != nc) ? nc.getCurrentDatabase() : null;
	}

	// configDb
	public Database getConfigDb() {
		if (configDb == null) {
			String config = "dbconfig.nsf";
			// TODO a 'dynamic' path should be calculated 
			String path = ""; 
			String filename = path + config;

			try {
				configDb = session.getDatabase("", filename);
			} catch (NotesException e) {
				debug.error(e.toString());
			}
		}

		return configDb;
	}

	public String getConfigUrl() {
		getConfigDb();
		String url = "";
		if (null != configDb) {
			try {
				url = configDb.getFilePath();
			} catch (NotesException e) {
				debug.error(e.toString());
			}
		}
		return url;
	}

	// session
	public Session getSession() {
		return session;
	}

	// currentDb
	public Database getCurrentDb() {
		return currentDb;
	}

	public String getCurrentDbUrl() {
		String s = "";
		try {
			s = currentDb.getFilePath();
		} catch (NotesException e) {
			debug.error(e.toString());
		}
		return s;
	}

	// Utils
	public String encode(String url) {
		try {
			url = URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			debug.error(e.toString());
		}
		return url;
	}

	// Methods
	public Database getDatabase(String id) {
		Database db = null;
		try {
			getConfigDb();
			View view = configDb.getView("admin.lookup");
			Document doc = view.getDocumentByKey(id);
			String path = doc.getItemValueString("path");
			path = path.substring(1); // remove trailing slash
			db = session.getDatabase("", path);

		} catch (NotesException e) {
			debug.error(e.toString());
		}
		return db;
	}

	public String getDatabaseUrl(String id) {
		String url = "";
		try {
			Database db = getDatabase(id);

			if (null != db) {
				url = db.getFilePath();
			}
		} catch (NotesException e) {
			debug.error(e.toString());
		}

		return url;
	}
	
	public static String getDatabaseURL(Database db) throws NotesException {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context
				.getExternalContext().getRequest();

		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = db.getFilePath();
		;
		return scheme + "://" + serverName
				+ ((serverPort == 80) ? "" : ":" + serverPort) + "/"
				+ contextPath;
	}

	public static String getDatabaseURL() {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context
				.getExternalContext().getRequest();
		String scheme = request.getScheme();
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String contextPath = request.getContextPath();
		return scheme + "://" + serverName
				+ ((serverPort == 80) ? "" : ":" + serverPort) + contextPath;
	}
}
