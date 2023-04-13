package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;

import pt.unl.fct.di.apdc.firstwebapp.util.DeleteRequest;

@Path("/delete")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DeleteResource {
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public DeleteResource() {
	}

	@DELETE
	@Path("/OP2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response removeUser(DeleteRequest request) {
		if (request.token == null)
			return Response.status(Status.UNAUTHORIZED).entity("Missing token").build();

		if (request.usernameToDelete == null || request.usernameToDelete.equals(""))
			return Response.status(Status.BAD_REQUEST).entity("Missing target username").build();
		
		Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", request.token.username))
				.setKind("Token").newKey(request.token.tokenID);
		Transaction txn = datastore.newTransaction();
		try {
			// Get token information
			Entity tokenEntity = txn.get(tokenKey);
			if (tokenEntity == null) {
				return Response.status(Status.UNAUTHORIZED).entity("Token not found").build();
			}
			if (!request.token.isMyToken(tokenEntity))
				return Response.status(Status.UNAUTHORIZED).entity("Token not valid").build();

			if (System.currentTimeMillis() >= request.token.expirationDate) {
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}

			String requesterUsername = request.token.username;
			String requesterRole = request.token.role;
			String usernameToDelete = request.usernameToDelete;

			// Get user to remove
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(usernameToDelete);
			Entity userEntity = txn.get(userKey);
			if (userEntity == null) {
				return Response.status(Status.NOT_FOUND).entity("User not found").build();
			}

			String userRole = userEntity.getString("role");

			// Check authorization
			boolean authorized = isAuthorizedToRemoveUser(requesterRole, userRole, requesterUsername, usernameToDelete);
			if (!authorized) {
				return Response.status(Status.FORBIDDEN).entity("Not authorized to remove this user").build();
			}

			// Remove user
			txn.delete(userKey);
			txn.commit();
			return Response.ok("User removed").build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private boolean isAuthorizedToRemoveUser(String requesterRole, String userRole, String requesterUsername,
			String username) {
		if (requesterUsername.equals(username)) {
			return true;
		}

		switch (requesterRole) {
		case "USER":
			return false;
		case "GBO":
			return userRole.equals("USER");
		case "GA":
			return userRole.equals("USER") || userRole.equals("GBO");
		case "GS":
			return userRole.equals("USER") || userRole.equals("GBO") || userRole.equals("GA");
		case "SU":
			return true;
		default:
			return false;
		}
	}

}
