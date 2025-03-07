package si.deisinger.business.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import si.deisinger.business.entity.ChargingStationsEntity;
import si.deisinger.business.repository.ChargingStationsRepository;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Path("/stations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChargingStationsController {

    private final ChargingStationsRepository repository;

    public ChargingStationsController(ChargingStationsRepository repository) {
        this.repository = repository;
    }

    @GET
    public List<ChargingStationsEntity> getAllStations() {
        return repository.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getStationById(@PathParam("id") Long id) {
        return repository.findByIdOptional(id).map(station -> Response.ok(station).build()).orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Transactional
    public Response createStation(ChargingStationsEntity station) {
        repository.persist(station);
        return Response.status(Response.Status.CREATED).entity(station).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateStation(@PathParam("id") Long id, ChargingStationsEntity updatedStation) {
        Optional<ChargingStationsEntity> existingStation = repository.findByIdOptional(id);
        if (existingStation.isPresent()) {
            ChargingStationsEntity station = existingStation.get();
            station.setStationId(updatedStation.getStationId());
            station.setProvider(updatedStation.getProvider());
            station.setFriendlyName(updatedStation.getFriendlyName());
            station.setAddress(updatedStation.getAddress());
            station.setLocation(updatedStation.getLocation());
            repository.persist(station);
            return Response.ok(station).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteStation(@PathParam("id") Long id) {
        boolean deleted = repository.deleteById(id);
        return deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }
}
