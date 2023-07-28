package com.cdac.enrollmentstation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author athisii, CDAC
 * Created on 29/03/23
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CRVerifyCertificateReqDto {
    @JsonProperty("handle")
    int handle;
    @JsonProperty("whichtrust")
    int whichTrust;
    @JsonProperty("whichcertificate")
    int whichCertificate;
    @JsonProperty("CertificateChain")
    String certificateChain;
    @JsonProperty("CertificateChain_len")
    int certificateChainLength;
}
