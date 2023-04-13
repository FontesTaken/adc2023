package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Context;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginRequest;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	// Logger Object
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	// GSON
	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoginResource() {
	} // Nothing to see here xD

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginRequest data) {
		LOG.fine("Login attempt by user: " + data.username);
		if (data.username.equals("jleitao") && data.password.equals("password")) {
			AuthToken at = new AuthToken(data.username, "nada");
			return Response.ok(g.toJson(at)).build();
		}
		return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
	}

	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
		if (username.equals("jleitao")) {
			return Response.ok().entity(g.toJson(false)).build();
		} else {
			return Response.ok().entity(g.toJson(true)).build();
		}
	}

	@POST
	@Path("/v1") // Tarefa 3
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loginUser(LoginRequest data) {
		LOG.fine("Attempt to login user: " + data.username);
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity userEntity = datastore.get(userKey);
		if (userEntity != null) {
			String hashedPwd = userEntity.getString("password");
			if (DigestUtils.sha512Hex(data.password).equals(hashedPwd)) {
				AuthToken at = new AuthToken(data.username, userEntity.getString("email"));
				LOG.info("User " + data.username + " logged in sucessfully");
				return Response.ok(g.toJson(at)).build();
			} else {
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} else {
			LOG.warning("Failed login attempt for username: " + data.username);
			return Response.status(Status.FORBIDDEN).build();
		}
	}

	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loginUser2(LoginRequest data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
		LOG.fine("Attempt to login user: " + data.username);
		// KEYS SHOULD BE GENERATED OUTSIDE TRANSACTIONS
		// Construct the key from the username
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Key ctrsKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username)).setKind("UserStats")
				.newKey("counters");
		// Generate automatically a key
		Key logKey = datastore.allocateId(datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
				.setKind("UserLog").newKey());
		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				// Username does not exist
				LOG.warning("Failed login attempt for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
			// We get the user stats from the storage
			Entity stats = txn.get(ctrsKey);
			if (stats == null) {
				stats = Entity.newBuilder(ctrsKey).set("user_stats_logins", 0L).set("user_stats_failed", 0L)
						.set("user_first_login", Timestamp.now()).set("user_last_login", Timestamp.now()).build();
			}
			String hashedPWD = (String) user.getString("password");
			if (hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				// Password is correct
				// Construct the logs
				Entity log = Entity.newBuilder(logKey).set("user_login_ip", request.getRemoteAddr())
						.set("user_login_host", request.getRemoteHost()).set("user_login_latlon",
								// Does not index this property value
								StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong"))
										.setExcludeFromIndexes(true).build())
						.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
						.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
						.set("user_login_time", Timestamp.now()).build();
				// Get the user statistics and updates it
				// Copying information every time a user logins maybe
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 1L + stats.getLong("user_stats_logins")).set("user_stats_failed", 0L)
						.set("user_first_login", stats.getLong("user_first_login"))
						.set("user_last_login", Timestamp.now()).build();

				// Batch operation
				txn.put(log, ustats);
				txn.commit();

				// Return token
				AuthToken at = new AuthToken(data.username, user.getString("email"));
				LOG.info("User '" + data.username + "' logged in sucessfully");
				return Response.ok(g.toJson(at)).build();
			} else {
				// Incorrect password
				// Compying here is even woerse. Propose a better solution!
				Entity ustats = Entity.newBuilder(ctrsKey).set("user_stats_logins", stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 1L + stats.getLong("user_stats_failed"))
						.set("user_first_login", stats.getLong("user_first_login"))
						.set("user_last_login", stats.getLong("user_last_login"))
						.set("user_last_attempt", Timestamp.now()).build();
				txn.put(ustats);
				txn.commit();
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@POST
	@Path("/OP6")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(LoginRequest request) {
		if (request == null || request.username == null || request.password == null) {
			return Response.status(Status.BAD_REQUEST).entity("Missing username or password").build();
		}

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(request.username);
		Transaction txn = datastore.newTransaction();
		try {
			Entity userEntity = txn.get(userKey);
			if (userEntity != null && DigestUtils.sha512Hex(request.password).equals(userEntity.getString("password"))
					&& userEntity.getString("estado").equals("ATIVO")) {
				String role = userEntity.getString("role");
				AuthToken authToken = new AuthToken(request.username, role);

				// Save token in the Datastore
				Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", request.username))
						.setKind("Token").newKey(authToken.tokenID);
				Entity tokenEntity = Entity.newBuilder(tokenKey).set("username", authToken.username)
						.set("role", authToken.role).set("creationDate", authToken.creationDate)
						.set("expirationDate", authToken.expirationDate).build();
				txn.put(tokenEntity);
				txn.commit();
				return Response.ok(g.toJson(authToken)).build();
			} else {
				return Response.status(Status.UNAUTHORIZED).entity("Invalid credentials").build();
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

}
