package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginRequest;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {


	// GSON
	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoginResource() {
	} // Nothing to see here xD

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
