package cmmv.backend

import grails.gorm.transactions.Transactional

import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.Sql
import com.twilio.Twilio
import mz.org.fgh.cmmv.backend.clinic.Clinic
import mz.org.fgh.cmmv.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.cmmv.backend.messages.FrontlineSmsDetails
import mz.org.fgh.cmmv.backend.messages.FrontlineSmsDetailsService
import mz.org.fgh.cmmv.backend.messages.TwilioDetail
import mz.org.fgh.cmmv.backend.messages.TwilioDetailService
import mz.org.fgh.cmmv.backend.mobilizer.CommunityMobilizer;
import mz.org.fgh.cmmv.backend.protection.SecRole
import mz.org.fgh.cmmv.backend.protection.SecUser
import mz.org.fgh.cmmv.backend.protection.SecUserSecRole
import mz.org.fgh.cmmv.backend.userLogin.MobilizerLogin
import mz.org.fgh.cmmv.backend.userLogin.MobilizerLoginService
import mz.org.fgh.cmmv.backend.userLogin.UserLogin

import javax.persistence.EntityNotFoundException

class BootStrap {

    public static final String REST_API_URL = "http://dev.fgh.org.mz:8180/api/1/webhook"
    public static final String API_KEY = "60205d65-8824-4f08-b915-ed28cbdd7430"

    FrontlineSmsDetailsService frontlineSmsDetailsService
    MobilizerLoginService mobilizerLoginService

    def init = { servletContext ->
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        addInitialUsers()
        addFrontlineSmsDetails()
     //   def frontLineSmsDetail = frontlineSmsDetailsService.list().get(0)
      //  Twilio.init(twilioDetails.getAccountSid(),twilioDetails.getAuthToken());
    }
    def destroy = {
    }

    @Transactional
    void addInitialUsers(){

        SpringSecurityUtils.clientRegisterFilter("corsFilterTest", SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order - 1)

        def adminRole = new SecRole('ROLE_ADMIN').save()
        def userRole = new SecRole('ROLE_USER').save()
        def mobilizerRole = new SecRole('ROLE_MOBILIZER').save()
        def districtRole = new SecRole('ROLE_USER_DISTRICT').save()

        def testUser = new SecUser('user', 'password','user').save()
        def adminUser = new SecUser('admin', 'admin','admin').save()

        def totalReg = CommunityMobilizer.count()
        def comMobilizer
        def mobLogin = MobilizerLogin.findByUsername('admin1')
        if (totalReg == 1) {
            comMobilizer = new CommunityMobilizer(firstNames: 'Adao', lastNames: 'Geneses', cellNumber: '840000000', cellNumber2: '860000000', uuid: '123456789-123456-asdqeasd-4sdase', clinic: Clinic.first()).save()

            if(!mobLogin){

                mobLogin = new MobilizerLogin()
                mobLogin.username = 'admin1'
                mobLogin.password = 'admin1'
                mobLogin.fullName = 'Adao Geneses'
                mobLogin.mobilizer = comMobilizer
                mobLogin.province = Province.first()
                mobLogin.district = mobLogin.province.districts.first()

                mobilizerLoginService.save(mobLogin)
            }

        }

        def clinicUser = new UserLogin()
        clinicUser.username = 'clinic'
        clinicUser.password = 'clinic'
        clinicUser.fullName = 'Clinic_Starter'
        clinicUser.clinic = Clinic.first()

        clinicUser.save()


        SecUserSecRole.create testUser, userRole
        SecUserSecRole.create adminUser, adminRole

        SecUserSecRole.create mobLogin as SecUser, adminRole
        SecUserSecRole.create clinicUser as SecUser, adminRole

        SecUserSecRole.withSession {
            it.flush()
            it.clear()
        }
    }

    @Transactional
    void addFrontlineSmsDetails(){

         def frontLineSmsDetail = frontlineSmsDetailsService.list()
         if(frontLineSmsDetail.size() <= 0) {
             def frontlineSms = new FrontlineSmsDetails(REST_API_URL,API_KEY);
             frontlineSmsDetailsService.save(frontlineSms)
         }

    }
}
