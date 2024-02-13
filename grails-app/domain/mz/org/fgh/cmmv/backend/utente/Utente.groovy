package mz.org.fgh.cmmv.backend.utente


import mz.org.fgh.cmmv.backend.address.Address
import mz.org.fgh.cmmv.backend.appointment.Appointment
import mz.org.fgh.cmmv.backend.clinic.Clinic
import mz.org.fgh.cmmv.backend.docsOrImages.InfoDocsOrImages
import mz.org.fgh.cmmv.backend.mobilizer.CommunityMobilizer
import mz.org.fgh.cmmv.backend.userLogin.UserLogin
import mz.org.fgh.cmmv.backend.utilities.Utilities

class Utente {

    String firstNames
    String lastNames
    Date birthDate
    String cellNumber
    String whatsappNumber
    String preferedLanguage
    String documentType
    String documentNumber
    String systemNumber
    Date registerDate
    String status
    String syncStatus

   // @JsonBackReference
    CommunityMobilizer communityMobilizer

   // Long  communityMobilizerId
   // @JsonIgnore
    UserLogin user

    boolean haspartner

  //  @JsonManagedReference
    static belongsTo = [clinic: Clinic]

    static hasMany = [infoDocsImages: InfoDocsOrImages, addresses:Address , appointments: Appointment]

    static fetchMode = [address: 'eager',communityMobilizer:'eager']

    static constraints = {
        lastNames(nullable: false, blank: false)
        birthDate(nullable: false, blank: true, validator: { birthDate, urc ->
            return birthDate != null ? birthDate <= new Date() : null
        })
        cellNumber(nullable: false, matches: /\d+/, maxSize: 12, minSize: 9, unique: ['firstNames','lastNames'])
        whatsappNumber(nullable: true, matches: /\d+/, maxSize: 12, minSize: 9)
        documentType(nullable: true, blank: true)
        documentNumber(nullable: true, blank: true)
        systemNumber(nullable: true, blank: true, unique: true)
        syncStatus(nullable: true)
        preferedLanguage(nullable: true)
        infoDocsImages(nullable: true)
        communityMobilizer(nullable: true)
        clinic(nullable: true)
        user(nullable: true)
    }

    def beforeInsert() {
        if (systemNumber == null) {
              //  def appointment = appointments.getAt(0)
            def month = Utilities.extractMonthInDate(registerDate)
            if (month < 10) month  = '0'+String.valueOf(month)
           // def month = Utilities.extractMonthInDate(appointment.appointmentDate)
            def year = String.valueOf(Utilities.extractYearInDate(registerDate))
            systemNumber = year+month+"-"+firstNames.substring(0,1)+lastNames.substring(0,1)+"-"+cellNumber
           println(systemNumber)
        }
    }
}

