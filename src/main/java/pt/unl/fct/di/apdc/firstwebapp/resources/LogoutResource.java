package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());

	public LogoutResource() {
	}

	@POST
	@Path("/OP8")
	public Response logout(AuthToken token) {
		if (token == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Missing token").build();
		}
		Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", token.username)).setKind("Token")
				.newKey(token.tokenID);
		Transaction txn = datastore.newTransaction();
		try {
			Entity tokenEntity = txn.get(tokenKey);
			if (tokenEntity == null) {
				return Response.status(Status.UNAUTHORIZED).entity("Token not found").build();
			}
			if (!token.isMyToken(tokenEntity))
				return Response.status(Status.UNAUTHORIZED).entity("Token not valid").build();

			if (System.currentTimeMillis() >= token.expirationDate) {
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}
			// Revoke token by removing it from the Datastore
			txn.delete(tokenKey);
			txn.commit();
			LOG.info("Logout successful for user: " + tokenEntity.getString("username"));
			return Response.ok("Logout successful").build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
}
