package com.integrationsi.hrapi.commit;

import com.hraccess.openhr.dossier.HRDossierCollection;

public interface IHrUpdateCommit {
	

    HrUpdateCommitResult commitDossiers(HRDossierCollection collection);

}
