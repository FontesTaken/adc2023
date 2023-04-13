package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.tasks.v2.*;
import com.google.gson.Gson;
import com.google.protobuf.Timestamp;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.UserDataToList;

import com.google.cloud.datastore.*;

@Path("/utils")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ComputationResource {

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ComputationResource() {
	} // nothing to be done here @GET

	// Metodos adicionais
	@POST
	@Path("/userPFP")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(AuthToken token) {
		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(token.username);
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

}