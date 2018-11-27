package io.cloudslang.content.postgres.actions;

import java.util.HashMap;
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
import static io.cloudslang.content.constants.OutputNames.STDERR;
import static io.cloudslang.content.constants.ReturnCodes.FAILURE;
import static io.cloudslang.content.constants.ReturnCodes.SUCCESS;
import static io.cloudslang.content.postgres.utils.Constants.*;
import static io.cloudslang.content.utils.OutputUtilities.getSuccessResultsMap;
import static io.cloudslang.content.utils.OutputUtilities.getFailureResultsMap;


public class UpdatePgHbaConfigAction {

    @Action(name = "Update pg_hba.config",
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
            @Param(value = ALLOWED_HOSTS) String allowedHosts,
            @Param(value = ALLOWED_USERS) String allowedUsers
    ) {

        try {
            if (allowedHosts == null || allowedHosts.trim().length() == 0) {
                return getFailureResultsMap("No changes in pg_hba.conf");
            }
            allowedHosts =  allowedHosts.replace("\'", "").trim();

            if(allowedUsers == null || allowedUsers.trim().length() == 0) {
                allowedUsers = "all";
            } else {
                allowedUsers = allowedUsers.replace("\'", "").trim();
            }

            ConfigService.changeProperty(installationPath, allowedHosts.split(";"), allowedUsers.split(";"));

            return getSuccessResultsMap("Updated pg_hba.conf successfully");
        } catch (Exception e) {
            return getFailureResultsMap("Failed to update pg_hba.conf", e);
        }
    }

}
