/**
 * Copyright 2014 Digitising Scotland project:
 * <http://digitisingscotland.cs.st-andrews.ac.uk/>
 *
 * This file is part of the module population_model.
 *
 * population_model is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population_model is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population_model. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.population_model.model.old;

import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPerson;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.database.old.DBBackedPartnership;

import java.sql.Date;
import java.util.List;

/*
 * An intermediate representation of a person.
 *
 * Has date of birth and date of death encoded fully but does not include family members etc.
 *
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 */
public class Person implements IPerson {

    private int id;
    private char sex;
    private String first_name;
    protected String surname;
    private Date birth_date;
    private Date death_date;
    private String occupation;
    private String cause_of_death;
    private String address;
    private String maiden_name;

    public Person() {
    }

    public Person(final int id, final char sex, final Date birth_date, final Date death_date, final String occupation, final String cause_of_death, final String address) {

        this(id, null, null, sex, birth_date, death_date, occupation, cause_of_death, address);
    }

    public Person(final int id, final String first_name, final String surname, final char sex, final Date birth_date, final Date death_date, final String occupation, final String cause_of_death, final String address) {

        this.sex = sex;
        this.first_name = first_name;
        this.surname = surname;
        this.id = id;
        this.birth_date = birth_date == null ? null : (Date) birth_date.clone();
        this.death_date = death_date == null ? null : (Date) death_date.clone();
        this.occupation = occupation;
        this.cause_of_death = cause_of_death;
        this.address = address;
    }

    public char getSex() {
        return sex;
    }

    public void setGender(final char sex) {

        if (!(sex == IPerson.FEMALE || sex == IPerson.MALE)) {
            throw new RuntimeException("illegal gender char");
        }
        this.sex = sex;
    }

    public String getFirstName() {

        return first_name;
    }

    public String getSurname() {

        return surname;
    }

    public void setSurname(final String surname) {

        this.surname = surname;
    }

    public void setFirstName(final String first_name) {

        this.first_name = first_name;
    }

    public Date getBirthDate() {

        return birth_date == null ? null : (Date) birth_date.clone();
    }

    public void setBirthDate(final Date birth_date) {

        this.birth_date = (Date) birth_date.clone();
    }

    public java.util.Date getDeathDate() {

        return death_date == null ? null : (Date) death_date.clone();
    }

    public void setDeathDate(final Date death_date) {

        this.death_date = (Date) death_date.clone();
    }

    public String getOccupation() {

        return occupation;
    }

    public void setOccupation(final String occupation) {

        this.occupation = occupation;
    }

    public String getCauseOfDeath() {

        return cause_of_death;
    }

    public void setCauseOfDeath(final String cause_of_death) {

        this.cause_of_death = cause_of_death;
    }

    public String getAddress() {

        return address;
    }

    @Override
    public List<Integer> getPartnerships() {
        throw new RuntimeException("unimplemented");
    }

    @Override
    public int getParentsPartnership() {
        throw new RuntimeException("unimplemented");
    }

    public void setAddress(final String address) {

        this.address = address;
    }

    public void setId(final int id) {

        this.id = id;
    }

    public int getId() {

        return id;
    }

    /**
     * Sets the Surname to be the new surname and the maiden name to be their old surname.
     */
    public void setMarriedName(final String new_surname) {

        maiden_name = surname;
        surname = new_surname;
    }

    /**
     * @return the maiden name of a person.
     */
    public String getMaidenName() {

        return maiden_name;
    }

//    @Override
//    public int getPartnership() {
//        return 0;
//    }
//
//    @Override
//    public int getParentsPartnership() {
//        return 0;
//    }

    public DBBackedPartnership getParentsFamily() {
        throw new RuntimeException("unimplemented");
    }

    public DBBackedPartnership getFamily() {
        throw new RuntimeException("unimplemented");
    }
}