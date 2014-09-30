package org.fastcatsearch.plugin;

/**
 * Created by swsong on 2014. 9. 30..
 */
public class PluginLicenseInfo {
    private String licenseProductName;
    private String licenseExpireDate;
    private String licensee;

    public PluginLicenseInfo(String licenseProductName, String licenseExpireDate, String licensee) {
        this.licenseProductName = licenseProductName;
        this.licenseExpireDate = licenseExpireDate;
        this.licensee = licensee;
    }

    public String getLicenseProductName() {
        return licenseProductName;
    }

    public String getLicenseExpireDate() {
        return licenseExpireDate;
    }

    public String getLicensee() {
        return licensee;
    }
}
