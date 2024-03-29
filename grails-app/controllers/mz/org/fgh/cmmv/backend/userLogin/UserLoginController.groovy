package mz.org.fgh.cmmv.backend.userLogin

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.cmmv.backend.clinic.Clinic
import mz.org.fgh.cmmv.backend.distribuicaoAdministrativa.District
import mz.org.fgh.cmmv.backend.mobilizer.CommunityMobilizer
import mz.org.fgh.cmmv.backend.protection.SecRole
import mz.org.fgh.cmmv.backend.protection.SecUserSecRole
import mz.org.fgh.cmmv.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class UserLoginController extends RestfulController{

    UserLoginService userLoginService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    UserLoginController() {
        super(UserLogin)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(userLoginService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(userLoginService.get(id)) as JSON
    }

    @Transactional
    def save() {
        UserLogin userLogin = new UserLogin()
        def objectJSON = request.JSON
        userLogin = objectJSON as UserLogin

        SecRole secRole = SecRole.findByAuthority('ROLE_USER')
        if (userLogin == null) {
            render status: NOT_FOUND
            return
        }
        if (userLogin.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond userLogin.errors
            return
        }

        try {
            userLoginService.save(userLogin)
            SecUserSecRole.create userLogin, secRole
        } catch (ValidationException e) {
            respond userLogin.errors
            return
        }

        respond userLogin, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(UserLogin userLogin) {
        if (userLogin == null) {
            render status: NOT_FOUND
            return
        }
        if (userLogin.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond userLogin.errors
            return
        }

        try {
            userLoginService.save(userLogin)
        } catch (ValidationException e) {
            respond userLogin.errors
            return
        }

        respond userLogin, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || userLoginService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def searchAllUsersByClinicId(Long clinicId){
        Clinic clinic = Clinic.findById(clinicId)
        render JSONSerializer.setObjectListJsonResponse(UserLogin.findAllByClinic(clinic)) as JSON
        // respond communityMobilizerService.getAllByDistrictId(districtId)
    }
}
