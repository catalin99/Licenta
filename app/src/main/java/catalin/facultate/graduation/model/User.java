package catalin.facultate.graduation.model;

import android.net.Uri;

import java.io.Serializable;
import java.time.LocalDate;

public class User implements Serializable {
    private String Email;
    private String CNP;
    private String PASSWORD;
    private String Nume;
    private String Prenume;
    private String Serie;
    private int Numar;
    private String Nationalitate;
    private String Judet;
    private String Municipiu;
    private String Localitate;
    private String Gender;
    private LocalDate DataNastere;
    private String UriImg1;
    private String UriImg2;
    private String UriImgCI;
    private String UserType;

    public User(String email, String cnp, String password) {
        Email = email;
        CNP = cnp;
        PASSWORD = password;
        UserType = "Normal";

        int genderExtract = Integer.parseInt(CNP.substring(0,1));
        if(genderExtract == 1 || genderExtract == 3 || genderExtract == 5 || genderExtract == 7)
            Gender = "Masculin";
        else
            Gender = "Feminin";

        int anExtract = Integer.parseInt(CNP.substring(1,3));
        int realAn = 0;
        if(genderExtract<3)
            realAn = 19;
        else if(genderExtract>2 && genderExtract<5)
            realAn = 18;
        else
            realAn = 20;
        DataNastere = LocalDate.of(realAn*100+anExtract, Integer.parseInt(CNP.substring(3,5)), Integer.parseInt(CNP.substring(5,7)));

    }

    public String getUserType() {
        return UserType;
    }

    public void setUserType(String userType) {
        UserType = userType;
    }

    public String getUriImg1() {
        return UriImg1;
    }

    public void setUriImg1(Uri uriImg1) {
        UriImg1 = uriImg1.toString();
    }

    public String getUriImg2() {
        return UriImg2;
    }

    public void setUriImg2(Uri uriImg2) {
        UriImg2 = uriImg2.toString();
    }

    public String getEmail() {
        return Email;
    }

    public String getCNP() {
        return CNP;
    }

    public String getSerie() {
        return Serie;
    }

    public void setSerie(String serie) {
        Serie = serie;
    }

    public int getNumar() {
        return Numar;
    }

    public void setNumar(int numar) {
        Numar = numar;
    }

    public String getNationalitate() {
        return Nationalitate;
    }

    public void setNationalitate(String nationalitate) {
        Nationalitate = nationalitate;
    }

    public String getJudet() {
        return Judet;
    }

    public void setJudet(String judet) {
        Judet = judet;
    }

    public String getOras() {
        return Municipiu;
    }

    public void setOras(String oras) {
        Municipiu = oras;
    }

    public String getLocalitate() {
        return Localitate;
    }

    public void setLocalitate(String localitate) {
        Localitate = localitate;
    }

    public String getUriImgCI() {
        return UriImgCI;
    }

    public void setUriImgCI(Uri uriImgCI) {
        UriImgCI = uriImgCI.toString();
    }

    public String getNume() {
        return Nume;
    }

    public void setNume(String nume) {
        Nume = nume;
    }

    public String getPrenume() {
        return Prenume;
    }

    public void setPrenume(String prenume) {
        Prenume = prenume;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public void setPASSWORD(String PASSWORD) {
        this.PASSWORD = PASSWORD;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public LocalDate getDataNastere() {
        return DataNastere;
    }

    public void setDataNastere(LocalDate dataNastere) {
        DataNastere = dataNastere;
    }
}
