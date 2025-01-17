package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.DELETE;

import com.google.gson.Gson;
import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Path("/utils")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ComputationResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final Gson g = new Gson();

	public ComputationResource() {
	} // nothing to be done here @GET

	// Metodos adicionais
	@POST
	@Path("/userPFP")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserPFP(AuthToken token) {
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.username);
		Transaction txn = datastore.newTransaction();
		try {
			Entity userEntity = txn.get(userKey);
			if (userEntity == null) {
				return Response.status(Status.NOT_FOUND).entity("User not found").build();
			}
			txn.rollback();
			return Response.ok(userEntity.getString("pfpURL")).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	@POST
	@Path("/giveUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(AuthToken token) {
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.username);
		Transaction txn = datastore.newTransaction();
		try {
			Entity userEntity = txn.get(userKey);
			if (userEntity == null) {
				return Response.status(Status.NOT_FOUND).entity("User not found").build();
			}
			txn.rollback();
			return Response.ok(g.toJson(userEntity)).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	@DELETE
	@Path("/deleteAllEntities")
	public Response deleteAllEntities() {
		Query<Entity> query = Query.newEntityQueryBuilder().build();
		QueryResults<Entity> results = datastore.run(query);

		while (results.hasNext()) {
			Entity entity = results.next();
			Key key = entity.getKey();

			Transaction txn = datastore.newTransaction();
			try {
				txn.delete(key);
				txn.commit();
			} finally {
				if (txn.isActive()) {
					txn.rollback();
				}
			}
		}

		return Response.ok("Todas as entidades foram excluídas.").build();
	}

}