package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.MessageRequest;
import pt.unl.fct.di.apdc.firstwebapp.util.MessageData;

import com.google.cloud.datastore.*;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class FeedResource {

	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public FeedResource() {

	}

	@POST
	@Path("/OP9")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createMessage(MessageRequest messageR) {
		if (messageR.token == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Missing token").build();
		}
		if (messageR.content == null || messageR.nameOfUser == null) {
			return Response.status(Status.BAD_REQUEST).entity("Missing details").build();
		}
		Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", messageR.token.username))
				.setKind("Token").newKey(messageR.token.tokenID);
		Transaction txn = datastore.newTransaction();
		try {
			Entity tokenEntity = txn.get(tokenKey);
			if (tokenEntity == null) {
				return Response.status(Status.UNAUTHORIZED).entity("Token not found").build();
			}
			if (!messageR.token.isMyToken(tokenEntity))
				return Response.status(Status.UNAUTHORIZED).entity("Token not valid").build();

			if (System.currentTimeMillis() >= messageR.token.expirationDate) {
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}
			MessageData msg = new MessageData(messageR.token.username, messageR.nameOfUser, messageR.content);
			if (msg.isMessageTooLong())
				return Response.status(Status.BAD_REQUEST).entity("Message too long").build();
			if (messageR.token == null) {
				return Response.status(Status.UNAUTHORIZED).entity("Missing token").build();
			}
			Key messageKey = datastore.newKeyFactory().setKind("Message").newKey(msg.messageID);
			Entity messageEntity = Entity.newBuilder(messageKey).set("username", msg.username)
					.set("nameOfUser", msg.nameOfUser).set("content", msg.content)
					.set("creationDate", msg.creationDate).build();
			txn.put(messageEntity);
			txn.commit();
			return Response.ok(g.toJson(messageEntity)).build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

}
