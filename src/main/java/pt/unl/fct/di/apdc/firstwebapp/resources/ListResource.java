package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.MessageData;
import pt.unl.fct.di.apdc.firstwebapp.util.UserDataToList;

import com.google.cloud.datastore.*;

@Path("/list")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListResource {

	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public ListResource() {
		
	}
	
	@POST
	@Path("/OP3")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response ListUser(AuthToken token) {
		if (token == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Missing token").build();
		}
		Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", token.username)).setKind("Token")
				.newKey(token.tokenID);
		Transaction txn = datastore.newTransaction();
		try {
			// Get token information
			Entity tokenEntity = txn.get(tokenKey);
			if (tokenEntity == null) {
				return Response.status(Status.UNAUTHORIZED).entity("Token not found").build();
			}
			if (!token.isMyToken(tokenEntity))
				return Response.status(Status.UNAUTHORIZED).entity("Token not valid").build();

			if (System.currentTimeMillis() >= token.expirationDate) {
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}
			List<UserDataToList> userDataList = new ArrayList<>();
			String role = token.role;

			if (role.equals("USER")) {
				Query<Entity> query = Query.newEntityQueryBuilder().setKind("User")
						.setFilter(CompositeFilter.and(PropertyFilter.eq("role", "USER"),
								PropertyFilter.eq("revealPerfil", true), PropertyFilter.eq("estado", "ATIVO")))
						.build();

				QueryResults<Entity> results = datastore.run(query);

				userDataList = userQueryToList(results);
				return Response.ok(g.toJson(userDataList)).build();

			} else if (role.equals("GBO")) {
				Query<Entity> queryUser = createQuery("USER");

				QueryResults<Entity> resultsUser = datastore.run(queryUser);

				userDataList = queryToList(resultsUser);
				return Response.ok(g.toJson(userDataList)).build();

			} else if (role.equals("GA")) {
				Query<Entity> queryUser = createQuery("USER");
				Query<Entity> queryGBO = createQuery("GBO");

				QueryResults<Entity> resultsUser = datastore.run(queryUser);
				QueryResults<Entity> resultsGBO = datastore.run(queryGBO);

				userDataList = queryToList(resultsUser);
				userDataList.addAll(queryToList(resultsGBO));

				return Response.ok(g.toJson(userDataList)).build();

			} else if (role.equals("GS")) {
				Query<Entity> queryUser = createQuery("USER");
				Query<Entity> queryGBO = createQuery("GBO");
				Query<Entity> queryGA = createQuery("GA");

				QueryResults<Entity> resultsUser = datastore.run(queryUser);
				QueryResults<Entity> resultsGBO = datastore.run(queryGBO);
				QueryResults<Entity> resultsGA = datastore.run(queryGA);

				userDataList = queryToList(resultsUser);
				userDataList.addAll(queryToList(resultsGBO));
				userDataList.addAll(queryToList(resultsGA));

				return Response.ok(g.toJson(userDataList)).build();

			} else if (role.equals("SU")) {
				Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();

				QueryResults<Entity> results = datastore.run(query);

				userDataList = queryToList(results);

				return Response.ok(g.toJson(userDataList)).build();
			}
			return Response.status(Status.NOT_FOUND).entity("Role doesnt exist").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}


	private Query<Entity> createQuery(String role) {
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").setFilter(PropertyFilter.eq("role", role))
				.build();

		return query;
	}

	private List<UserDataToList> userQueryToList(QueryResults<Entity> results) {
		List<UserDataToList> userDataList = new ArrayList<>();
		while (results.hasNext()) {
			Entity entity = results.next();
			Key test = entity.getKey();
			String username = test.getName();
			String email = entity.getString("email");
			String name = entity.getString("name");
			UserDataToList toFill = new UserDataToList(username, email, name);
			userDataList.add(toFill);
		}
		return userDataList;
	}

	private List<UserDataToList> queryToList(QueryResults<Entity> results) {
		List<UserDataToList> userDataList = new ArrayList<>();
		while (results.hasNext()) {
			Entity entity = results.next();
			Key test = entity.getKey();
			String username = test.getName();
			String email = entity.getString("email");
			String name = entity.getString("name");
			UserDataToList toFill = new UserDataToList(username, email, name);
			for (String property : entity.getNames()) {
				if (!property.equals("email") && !property.equals("name")) {
					Value<?> value = entity.getValue(property);
					toFill.addValue(property, value);
				}
			}
			userDataList.add(toFill);
		}
		return userDataList;
	}

	@POST
	@Path("/OP10")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response listMessages(AuthToken token) {
		if (token == null) {
			return Response.status(Status.UNAUTHORIZED).entity("Missing token").build();
		}
		Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", token.username)).setKind("Token")
				.newKey(token.tokenID);
		Transaction txn = datastore.newTransaction();
		try {
			// Get token information
			Entity tokenEntity = txn.get(tokenKey);
			if (tokenEntity == null) {
				return Response.status(Status.UNAUTHORIZED).entity("Token not found").build();
			}
			if (!token.isMyToken(tokenEntity))
				return Response.status(Status.UNAUTHORIZED).entity("Token not valid").build();

			if (System.currentTimeMillis() >= token.expirationDate) {
				return Response.status(Status.UNAUTHORIZED).entity("Token expired").build();
			}
			List<MessageData> messages = new ArrayList<>();
			Query<Entity> query = Query.newEntityQueryBuilder().setKind("Message")
					.setOrderBy(OrderBy.desc("creationDate")).build();

			QueryResults<Entity> results = datastore.run(query);
			String role = tokenEntity.getString("role");
			while (results.hasNext()) {
				Entity entity = results.next();
				MessageData message = new MessageData();
				message.messageID = entity.getKey().getName();
				message.username = entity.getString("username");
				message.nameOfUser = entity.getString("nameOfUser");
				message.content = entity.getString("content");
				message.creationDate = entity.getLong("creationDate");

				if (!role.equals("USER")) {
					messages.add(message);
				} else {
					MessageData userMessage = new MessageData();
					userMessage.username = message.username;
					userMessage.nameOfUser = message.nameOfUser;
					userMessage.content = message.content;
					userMessage.creationDate = message.creationDate;
					messages.add(userMessage);
				}
			}
			return Response.ok(g.toJson(messages)).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

}
