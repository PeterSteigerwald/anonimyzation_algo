package com.steigerwald;

public class StructureData {

    public void anonymizePatient() {

        anonymizeIdentifier();
        anonymizeActive();
        anonymizeName();
        anonymizeTelecom();
        anonymizeGender();
        anonymizeBirthDate();
        anonymizeDeceased();
        anonymizeAddress();
        anonymizeMaritalStatus();
        anonymizeMultipleBirth();
        anonymizePhoto();
        anonymizeContact();
        anonymizeCommunication();
        anonymizeGeneralPractitioner();
        anonymizeManagingOrganization();
        anonymizeLink();
    }

    public void anonymizeIdentifier() {
        /*
         * Identifier
         * use : code [0..1] « IdentifierUse! »
         * type : CodeableConcept [0..1] « IdentifierTypeCodes+ »
         * system : uri [0..1]
         * value : string [0..1]
         * period : Period [0..1]
         * assigner : Reference [0..1] « Organization »
         */

        // <identifier><!-- 0..* Identifier An identifier for this patient
        // --></identifier>
    }

    public void anonymizeActive() {

        /*
         * value = boolean
         */

        // <active value="[boolean]"/><!-- 0..1 Whether this patient's record is in
        // active use -->
    }

    public void anonymizeName() {
        /*
         * HumanName
         * use: code
         * text: string
         * family: string
         * given: string
         * prefix: string
         * suffix: string
         * period: Period
         */

        // <name><!-- 0..* HumanName A name associated with the patient --></name>
    }

    public void anonymizeTelecom() {

        /*
         * ContactPoint
         * system : code [0..1] « ContactPointSystem! »
         * value : string [0..1] « C »
         * use : code [0..1] « ContactPointUse! »
         * rank : positiveInt [0..1]
         * period : Period [0..1]
         */

        // <telecom><!-- 0..* ContactPoint A contact detail for the individual
        // --></telecom>
    }

    public void anonymizeGender() {

        /*
         * male | female | other | unknown
         */

        // <gender value="[code]"/><!-- 0..1 male | female | other | unknown -->
    }

    public void anonymizeBirthDate() {

        /*
         * value = date
         */

        // <birthDate value="[date]"/><!-- 0..1 The date of birth for the individual
        // -->}
    }

    public void anonymizeDeceased() {

        // <deceased[x]><!-- 0..1 boolean|dateTime Indicates if the individual is
        // deceased or not --></deceased[x]>
    }

    public void anonymizeAddress() {

        /*
         * Address
         * use : code [0..1] « AddressUse! »
         * type : code [0..1] « AddressType! »
         * text : string [0..1]
         * line : string [0..*]
         * city : string [0..1]
         * district : string [0..1]
         * state : string [0..1]
         * postalCode : string [0..1]
         * country : string [0..1]
         * period : Period [0..1]
         */
        // <address><!-- 0..* Address An address for the individual --></address>
    }

    public void anonymizeMaritalStatus() {

        /*
         * http://hl7.org/fhir/R5/valueset-marital-status.html for further information
         * about the codes
         * UKN is unknown
         */

        // <maritalStatus><!-- 0..1 CodeableConcept Marital (civil) status of a patient
        // --></maritalStatus>
    }

    public void anonymizeMultipleBirth() {

        // <multipleBirth[x]><!-- 0..1 boolean|integer Whether patient is part of a
        // multiple birth --></multipleBirth[x]>
    }

    public void anonymizePhoto() {

        /*
         * Attachment
         * contentType : code [0..1] « MimeTypes! »
         * language : code [0..1] « AllLanguages! »
         * data : base64Binary [0..1]
         * url : url [0..1]
         * size : integer64 [0..1]
         * hash : base64Binary [0..1]
         * title : string [0..1]
         * creation : dateTime [0..1]
         * height : positiveInt [0..1]
         * width : positiveInt [0..1]
         * frames : positiveInt [0..1]
         * duration : decimal [0..1]
         * pages : positiveInt [0..1]
         */
        // <photo><!-- 0..* Attachment Image of the patient --></photo>
    }

    public void anonymizePeriod() {

        /*
         * Example Structure:
         * <coverage>
         * <start value="2011-05-23" />
         * <end value="2011-05-27" />
         * </coverage>
         */
        // <period><!-- 0..1 Period The period during which this contact person or
        // organization
        // is valid to be contacted relating to this patient --></period>
    }

    public void anonymizeRelationship() {

        /*
         * http://hl7.org/fhir/R5/valueset-patient-contactrelationship.html for more
         * information
         * U = unknown
         * 
         */

        // <relationship><!-- 0..* CodeableConcept The kind of relationship
        // --></relationship>
    }

    public void anonymizeOrganization() {

        // <organization><!-- I 0..1 Reference(Organization) Organization that is
        // associated with the
        // contact --></organization>
    }

    public void anonymizeLanguage() {
        // <language><!-- 1..1 CodeableConcept The language which can be used to
        // communicate with
        // the patient about his or her health --></language>

    }

    public void anonymizePreferred() {
        // <preferred value="[boolean]"/><!-- 0..1 Language preference indicator -->
    }

    public void anonymizeGeneralPractitioner() {
        // <generalPractitioner><!-- 0..*
        // Reference(Organization|Practitioner|PractitionerRole) Patient's nominated
        // primary care provider --></generalPractitioner>
    }

    public void anonymizeManagingOrganization() {
        // <managingOrganization><!-- 0..1 Reference(Organization) Organization that is
        // the custodian of the patient record --></managingOrganization>
    }

    public void anonymizeOther() {
        // <other><!-- 1..1 Reference(Patient|RelatedPerson) The other patient or
        // related person resource that the link refers to --></other>
    }

    public void anonymizeType() {
        // <type value="[code]"/><!-- 1..1 replaced-by | replaces | refer | seealso -->
    }

    /**
     * @param i++
     */
    public void anonymizeLink() { // <link> <!-- 0..* Link to a Patient or RelatedPerson resource that concerns
                                  // the same actual individual -->
        int linkCount = 10; // TODO :Placeholder
        for (int i = 1; linkCount >= i; i++) {

            anonymizeOther(); // <language><!-- 1..1 CodeableConcept The language which can be used to
                              // communicate with the patient about his or her health --></language>
            anonymizeType(); // <type value="[code]"/><!-- 1..1 replaced-by | replaces | refer | seealso -->
        }
    }

    public void anonymizeCommunication() {// <communication> <!-- 0..* A language which may be used to communicate with
                                          // the patient about his or her health -->

        int languageCount = 10; // TODO :Placeholder
        for (int i = 1; languageCount >= i; i++) {

            anonymizeLanguage(); // <language><!-- 1..1 CodeableConcept The language which can be used to
                                 // communicate with the patient about his or her health --></language>
            anonymizePreferred();// <preferred value="[boolean]"/><!-- 0..1 Language preference indicator -->
        }

    }

    public void anonymizeContact() {// <!-- 0..* A contact party (e.g. guardian, partner, friend) for the patient
                                    // -->
        anonymizeRelationship(); // <!-- 0..* CodeableConcept The kind of relationship --></relationship>
        anonymizeName(); // <!-- I 0..1 HumanName A name associated with the contact person --></name>
        anonymizeTelecom(); // <!-- I 0..* ContactPoint A contact detail for the person --></telecom>
        anonymizeAddress();// <!-- I 0..1 Address Address for the contact person --></address>
        anonymizeGender(); // <!-- 0..1 male | female | other | unknown -->
        anonymizeOrganization(); // <!-- I 0..1 Reference(Organization) Organization that is associated with the
                                 // contact --></organization>
        anonymizePeriod(); // <!-- 0..1 Period The period during which this contact person or organization
                           // is valid to be contacted relating to this patient --></period>
        // do something
    }

}
