package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import pt.unl.fct.di.apdc.firstwebapp.util.UpdateAttributesData;
import pt.unl.fct.di.apdc.firstwebapp.util.UpdatePasswordData;
import pt.unl.fct.di.apdc.firstwebapp.util.UpdateUserAttributes;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;

@Path("/changeAttribute")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UserResource {
	
	private static final Logger LOG = Logger.getLogger(ComputationResource.class.getName());

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public UserResource () {
		
	}
	
	@POST
	@Path("/OP4")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response updateUserAttributes(UpdateAttributesData updateAttributesData) {
		if (updateAttributesData.token == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Missing token").build();
		}

		if (!updateAttributesData.isUsernameValid())
			return Response.status(Status.FORBIDDEN).entity("Username invalid").build();

		Key tokenKey = datastore.newKeyFactory()
				.addAncestor(PathElement.of("User", updateAttributesData.token.username)).setKind("Token")
				.newKey(updateAttributesData.token.tokenID);
		Key targetUserKey = datastore.newKeyFactory().setKind("User").newKey(updateAttributesData.targetUsername);
		Transaction txn = datastore.newTransaction();

		try {
			Entity tokenEntity = txn.get(tokenKey);
			if (tokenEntity == null) {
				return Response.status(Status.UNAUTHORIZED).entity("Token not found").build();
			}
			if (!updateAttributesData.token.isMyToken(tokenEntity))
				return Response.status(Status.UNAUTHORIZED).entity("Token not valid").build();

			if (System.currentTimeMillis() >= updateAttributesData.token.expirationDate) {
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}

			Entity targetUserEntity = txn.get(targetUserKey);

			if (targetUserEntity == null) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("User not found.").build();
			}

			if (System.currentTimeMillis() > updateAttributesData.token.expirationDate) {
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}

			String tokenUser = updateAttributesData.token.username;
			String tokenRole = updateAttributesData.token.role;
			String targetUserRole = targetUserEntity.getString("role");

			Entity.Builder userBuilder = Entity.newBuilder(targetUserEntity);

			if (tokenRole.equals("USER") && tokenUser.equals(updateAttributesData.targetUsername)) {
				updateUserBuilder(userBuilder, updateAttributesData.data, true);
			} else if (canModifyUser(tokenRole, targetUserRole)) {
				updateUserBuilder(userBuilder, updateAttributesData.data, false);
			} else {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("You do not have permission to modify this user.")
						.build();
			}

			txn.put(userBuilder.build());
			txn.commit();
			return Response.ok().entity("{\"message\": \"User attributes updated.\"}").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private boolean canModifyUser(String tokenRole, String targetUserRole) {
		switch (tokenRole) {
		case "GBO":
			return "USER".equals(targetUserRole);
		case "GA":
			return "GBO".equals(targetUserRole) || "USER".equals(targetUserRole);
		case "GS":
			return "GBO".equals(targetUserRole) || "GA".equals(targetUserRole) || "USER".equals(targetUserRole);
		case "SU":
			return true;
		default:
			return false;
		}
	}

	private void updateUserBuilder(Entity.Builder userBuilder, UpdateUserAttributes data, boolean isUser) {
		if (!isUser) {
			if (data.email != null) {
				if (data.email.equals(""))
					userBuilder.build().getString("email");
				else
					userBuilder.set("email", data.email);
			}
			if (data.name != null) {
				if (data.name.equals(""))
					userBuilder.build().getString("name");
				else
					userBuilder.set("name", data.name);
			}
			if (data.role != null) {
				if (data.role.equals(""))
					userBuilder.build().getString("role");
				else
					userBuilder.set("role", data.role);
			}
			if (data.estado != null) {
				if (data.estado.equals(""))
					userBuilder.build().getString("estado");
				else
					userBuilder.set("estado", data.estado);
			}
		}

		if (data.revealPerfil != null) {
			userBuilder.set("revealPerfil", data.revealPerfil);
		}
		if (data.telefone != null) {
			userBuilder.set("telefone", data.telefone);
		}
		if (data.telemovel != null) {
			userBuilder.set("telemovel", data.telemovel);
		}
		if (data.ocupacao != null && !data.ocupacao.equals("")) {
			userBuilder.set("ocupacao", data.ocupacao);
		}
		if (data.localTrabalho != null && !data.localTrabalho.equals("")) {
			userBuilder.set("localTrabalho", data.localTrabalho);
		}
		if (data.morada1 != null && !data.morada1.equals("")) {
			userBuilder.set("morada1", data.morada1);
		}
		if (data.morada2 != null && !data.morada2.equals("")) {
			userBuilder.set("morada2", data.morada2);
		}
		if (data.pfpURL != null && !data.pfpURL.equals("")) {
			userBuilder.set("pfpURL", data.pfpURL);
		}
		if (data.nif != null) {
			userBuilder.set("NIF", data.nif);
		}
	}

	@POST
	@Path("/OP5")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response updatePassword(UpdatePasswordData updatePasswordData) {
		if (updatePasswordData.token == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Missing token").build();
		}

		if (!updatePasswordData.data.isInputValid())
			return Response.status(Status.BAD_REQUEST).entity("Invalid input.").build();

		Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", updatePasswordData.token.username))
				.setKind("Token").newKey(updatePasswordData.token.tokenID);
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(updatePasswordData.token.username);
		Transaction txn = datastore.newTransaction();

		try {
			// Get token information
			Entity tokenEntity = txn.get(tokenKey);
			if (tokenEntity == null) {
				return Response.status(Status.UNAUTHORIZED).entity("Token not found").build();
			}
			if (!updatePasswordData.token.isMyToken(tokenEntity))
				return Response.status(Status.UNAUTHORIZED).entity("Token not valid").build();

			if (System.currentTimeMillis() >= updatePasswordData.token.expirationDate) {
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}

			Entity userEntity = txn.get(userKey);

			if (userEntity == null) {
				txn.rollback();
				return Response.status(Status.NOT_FOUND).entity("User not found.").build();
			}

			String currentHashedPassword = userEntity.getString("password");

			if (!currentHashedPassword.equals(DigestUtils.sha512Hex(updatePasswordData.data.oldPassword))) {
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("Incorrect current password.").build();
			}

			if (!updatePasswordData.data.isNewPasswordValid()) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Passwords do not match.").build();
			}

			Entity updatedUser = Entity.newBuilder(userEntity)
					.set("password", DigestUtils.sha512Hex(updatePasswordData.data.newPassword)).build();

			txn.put(updatedUser);
			txn.commit();
			return Response.ok("Password updated.").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

}
