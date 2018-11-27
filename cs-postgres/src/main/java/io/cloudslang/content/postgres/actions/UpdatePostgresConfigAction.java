package io.cloudslang.content.postgres.actions;

import java.util.Map;

import com.hp.oo.sdk.content.annotations.Action;
import com.hp.oo.sdk.content.annotations.Output;
import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.annotations.Response;
import com.hp.oo.sdk.content.plugin.ActionMetadata.MatchType;
import com.hp.oo.sdk.content.plugin.ActionMetadata.ResponseType;
import io.cloudslang.content.constants.ResponseNames;
import io.cloudslang.content.postgres.services.ConfigService;

import static io.cloudslang.content.constants.OutputNames.*;
import static io.cloudslang.content.constants.ReturnCodes.FAILURE;
import static io.cloudslang.content.constants.ReturnCodes.SUCCESS;
import static io.cloudslang.content.postgres.utils.Constants.*;
import static io.cloudslang.content.utils.OutputUtilities.getSuccessResultsMap;
import static io.cloudslang.content.utils.OutputUtilities.getFailureResultsMap;


public class UpdatePostgresConfigAction {

    @Action(name = "Update Property Value",
            outputs = {
                    @Output(RETURN_CODE),
                    @Output(RETURN_RESULT),
                    @Output(EXCEPTION),
                    @Output(STDERR)
            },
            responses = {
                    @Response(text = ResponseNames.SUCCESS, field = RETURN_CODE, value = SUCCESS,
                            matchType = MatchType.COMPARE_EQUAL,
                            responseType = ResponseType.RESOLVED),
                    @Response(text = ResponseNames.FAILURE, field = RETURN_CODE, value = FAILURE,
                            matchType = MatchType.COMPARE_EQUAL, responseType = ResponseType.ERROR, isOnFail = true)
            })
    public Map<String, String> execute(
            @Param(value = FILE_PATH, required = true) String installationPath,
            @Param(value = PORT) String port,
            @Param(value = SSL) String ssl,
            @Param(value = SSL_CA_FILE) String sslCaFile,
            @Param(value = SSL_CERT_FILE) String sslCertFile,
            @Param(value = SSL_KEY_FILE) String sslKeyFile,
            @Param(value = MAX_CONNECTIONS) String maxConnections,
            @Param(value = SHARED_BUFFERS) String sharedBuffers,
            @Param(value = EFFECTIVE_CACHE_SIZE) String effectiveCacheSize,
            @Param(value = AUTOVACUUM) String autovacuum,
            @Param(value = WORK_MEM) String workMem
    ) {

        try {
            Map<String, Object> keyValues = ConfigService.validateAndBuildKeyValuesMap(
                    port, ssl, sslCaFile, sslCertFile, sslKeyFile, maxConnections, sharedBuffers, effectiveCacheSize, autovacuum, workMem);

            ConfigService.changeProperty(installationPath, keyValues);

            return getSuccessResultsMap("Updated postgresql.conf successfully");
        } catch (Exception e) {
            return getFailureResultsMap("Failed to update postgresql.conf", e);
        }
    }

}
