package mz.org.fgh.cmmv.backend.clinic

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.cmmv.backend.distribuicaoAdministrativa.District
import mz.org.fgh.cmmv.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class ClinicController extends RestfulController{

    ClinicService clinicService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ClinicController() {
        super(Clinic)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(clinicService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(clinicService.get(id)) as JSON
    }


    @Transactional
    def save(Clinic clinic) {
        if (clinic == null) {
            render status: NOT_FOUND
            return
        }
        if (clinic.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinic.errors
            return
        }

        try {
            clinicService.save(clinic)
        } catch (ValidationException e) {
            respond clinic.errors
            return
        }

        respond clinic, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(Clinic clinic) {
        if (clinic == null) {
            render status: NOT_FOUND
            return
        }
        if (clinic.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinic.errors
            return
        }

        try {
            clinicService.save(clinic)
        } catch (ValidationException e) {
            respond clinic.errors
            return
        }

        respond clinic, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || clinicService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def search(double latitude) {
        respond clinicService.searchByDistance(-25.814740,32.561956,50)
    }

    def searchClinicsByDistrictId(Long districtId){
        District district = District.findById(districtId)
        render JSONSerializer.setObjectListJsonResponse(Clinic.findAllByDistrictAndActive(district,true)) as JSON
        // respond communityMobilizerService.getAllByDistrictId(districtId)
    }

    def findClinicsNearUser(Double userLatitude, Double userLongitude, Double radiusInKm) {
        def earthRadius = 6371 // Earth's radius in kilometers

        // Calculate the maximum and minimum latitude and longitude
        def maxLat = userLatitude + Math.toDegrees(radiusInKm / earthRadius)
        def minLat = userLatitude - Math.toDegrees(radiusInKm / earthRadius)
        def maxLon = userLongitude + Math.toDegrees(Math.asin(radiusInKm / earthRadius) / Math.cos(Math.toRadians(userLatitude)))
        def minLon = userLongitude - Math.toDegrees(Math.asin(radiusInKm / earthRadius) / Math.cos(Math.toRadians(userLatitude)))

        // Perform a GORM query to find clinics within the bounding box
        def nearbyClinics = Clinic.findAll {
            between('latitude', minLat, maxLat) && between('longitude', minLon, maxLon)  && eq('active', true)
        }

           render JSONSerializer.setObjectListJsonResponse(nearbyClinics) as JSON
        }

    def searchAllActiveClinics(){
            render JSONSerializer.setObjectListJsonResponse(Clinic.findAllByActive(true)) as JSON
    }
}
