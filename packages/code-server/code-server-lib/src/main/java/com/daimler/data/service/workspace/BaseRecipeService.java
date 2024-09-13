package com.daimler.data.service.workspace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.daimler.data.dto.workspace.recipe.SoftwareCollection;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.daimler.data.db.repo.workspace.WorkspaceCustomSoftwareRepo;
import com.daimler.data.assembler.AdditionalServiceAssembler;
import com.daimler.data.assembler.RecipeAssembler;
import com.daimler.data.db.entities.CodeServerAdditionalServiceNsql;
import com.daimler.data.db.entities.CodeServerRecipeNsql;
import com.daimler.data.db.repo.workspace.WorkspaceCustomAdditionalServiceRepo;
import com.daimler.data.db.repo.workspace.WorkspaceCustomAdditionalServiceRepoImpl;
import com.daimler.data.db.repo.workspace.WorkspaceCustomRecipeRepo;
import com.daimler.data.db.repo.workspace.WorkspaceRecipeRepository;
import com.daimler.data.dto.workspace.recipe.RecipeVO;
import com.daimler.data.db.json.CodeServerAdditionalService;
import com.daimler.data.db.json.CodeServerSoftware;
import lombok.extern.slf4j.Slf4j;
import com.daimler.data.assembler.SoftwareAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.CodeServerSoftwareNsql;
import java.util.UUID;
import com.daimler.data.dto.CodeServerRecipeDto;
import com.daimler.data.dto.CodeServerRecipeDto;
import com.daimler.data.dto.workspace.recipe.AdditionalPropertiesVO;
import com.daimler.data.dto.workspace.recipe.AdditionalServiceLovVo;
import com.daimler.data.dto.workspace.recipe.InitializeAdditionalServiceLovVo;
import com.daimler.data.dto.workspace.recipe.RecipeLovVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.daimler.data.dto.CodeServerRecipeDto;
import com.daimler.dna.notifications.common.producer.KafkaProducerService;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.client.GitClient;
import com.daimler.data.dto.solution.ChangeLogVO;
import org.springframework.http.HttpStatus;
import com.daimler.data.application.client.GitClient;


@Service
@Slf4j
@SuppressWarnings(value = "unused")
public class BaseRecipeService implements RecipeService{

	@Autowired
	private WorkspaceRecipeRepository recipeJpaRepo;

	@Autowired
	private RecipeAssembler recipeAssembler;

	@Autowired
	private WorkspaceCustomRecipeRepo workspaceCustomRecipeRepo;

	@Autowired
	private WorkspaceCustomSoftwareRepo workspaceCustomSoftwareRepo;

	@Autowired
	private SoftwareAssembler softwareAssembler;

	@Autowired
	private AdditionalServiceAssembler additionalServiceAssembler;

	@Autowired
	private KafkaProducerService kafkaProducer;

	@Autowired
	private WorkspaceCustomAdditionalServiceRepo additionalServiceRepo;

	@Autowired
	 private UserStore userStore;

	@Autowired
	private GitClient gitClient;
    
	@Override
	@Transactional
	public List<RecipeVO> getAllRecipes(int offset, int limit,String id) {
		List<CodeServerRecipeNsql> entities = workspaceCustomRecipeRepo.findAllRecipe(offset, limit,id);
		return entities.stream().map(n -> recipeAssembler.toVo(n)).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public RecipeVO createRecipe(RecipeVO recipeRequestVO) {
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		
		CodeServerRecipeNsql entity = recipeAssembler.toEntity(recipeRequestVO);
		CodeServerRecipeNsql savedEntity = new CodeServerRecipeNsql();
		try {
			Date now = isoFormat.parse(isoFormat.format(new Date()));
			entity.getData().setCreatedOn(now);
			savedEntity = recipeJpaRepo.save(entity);
		} catch (Exception e) {
			log.error("Failed in assembler while parsing date into iso format with exception {}", e.getMessage());
		}
		return recipeAssembler.toVo(savedEntity);
	}

	@Override
	public GenericMessage createOrValidateSoftwareTemplate(String gitHubUrl, List<String> softwares) {
		GenericMessage responseMessage = new GenericMessage();
		HttpStatus status = null;
		try {
			String repoName = null;
			String softwareFileName = null;
			String gitUrl = null;
			String repoOwner = null;
			String SHA = null;
			String encodedFileContent = null;
			StringBuffer fileContent =  new StringBuffer();
			if(gitHubUrl.contains(".git")) {
				gitHubUrl = gitHubUrl.replaceAll("\\.git$", "/");
			}
			String[] codespaceSplitValues = gitHubUrl.split("/");
			int length = codespaceSplitValues.length;
			repoName = codespaceSplitValues[length-1];
			repoOwner = codespaceSplitValues[length-2];
			gitUrl = gitHubUrl.replace("/"+repoOwner, "");
			gitUrl = gitUrl.replace("/"+repoName, "");
			JSONObject jsonResponse = gitClient.getSoftwareFileFromGit(repoName, repoOwner, gitUrl);
			if(jsonResponse !=null && jsonResponse.has("name") && jsonResponse.has("content")) {
				softwareFileName  = jsonResponse.getString("name");
				SHA =  jsonResponse.has("sha")? jsonResponse.getString("sha") : null;
				log.info("Retrieving a software's SHA was successfull from Git.");
			}
			for(String software: softwares) {
				String additionalProperties = workspaceCustomRecipeRepo.findBySoftwareName(software);
				fileContent.append(additionalProperties);
			}
			if(fileContent.toString().contains("dotnet")){
				fileContent.append("\ncode-server --install-extension ms-dotnettools.vscode-dotnet-runtime\ncode-server --install-extension aliasadidev.nugetpackagemanagergui");
			}
			fileContent.append("\ncode-server --install-extension mtxr.sqltools-driver-pg\ncode-server --install-extension mtxr.sqltools\ncode-server --install-extension cweijan.vscode-database-client2\ncode-server --install-extension cweijan.vscode-redis-client\n");
			encodedFileContent = Base64.getEncoder().encodeToString(fileContent.toString().getBytes());
			if( encodedFileContent != null) {
				status = gitClient.createOrValidateSoftwareInGit(repoName, repoOwner, SHA, gitUrl, encodedFileContent);
				if(status.is2xxSuccessful()) {
					log.info("Software creation was successfull in Git.");
					responseMessage.setSuccess("SUCCESS");
					return responseMessage;
				}
			}
			else {
				responseMessage = 	getMessageDescrption("An error occurred while encoding the software file into the Git repository.","FAILED");
				return responseMessage;
			}
			log.info("Software creation failed in Git.");
			responseMessage.setSuccess("FAILED");
			return responseMessage;
		} catch(Exception e) {
			log.error(e.getMessage());
			if(e.getMessage().contains("pull request")){
				responseMessage = getMessageDescrption("Conflict in Git while creating Software File","FAILED");
			} else {
				responseMessage = 	getMessageDescrption("An unexpected error occurred while uploading a software file to the Git repository.", "FAILED");

			}
			responseMessage.setSuccess("FAILED");
		}
		return responseMessage;
	}

	private GenericMessage getMessageDescrption(String message, String statusMsg ) {
		GenericMessage responseMessage = new GenericMessage();
		MessageDescription msg = new MessageDescription();
		List<MessageDescription> errorMessage = new ArrayList<>();
		msg.setMessage(message);
		errorMessage.add(msg);
		responseMessage.addErrors(msg);
		responseMessage.setSuccess(statusMsg);
		return responseMessage;
	}
	

	@Override
	public GenericMessage validateGitHubUrl(String gitHubUrl){
		GenericMessage responseMessage = new GenericMessage();
		responseMessage.setSuccess("SUCCESS");
		try
			{
				String repoName = null;
				String gitUrl = null;
				String applicationName = null;
				if(gitHubUrl.contains(".git")) {
					gitHubUrl = gitHubUrl.replaceAll("\\.git$", "/");
				}
				String[] codespaceSplitValues = gitHubUrl.split("/");
				int length = codespaceSplitValues.length;
				repoName = codespaceSplitValues[length-1];
				applicationName = codespaceSplitValues[length-2];
				gitUrl = gitHubUrl.replace("/"+codespaceSplitValues[length-1], "");
				gitUrl = gitUrl.replace("/"+codespaceSplitValues[length-2], "");
            	HttpStatus validateUserPatstatus = gitClient.validateGitUser(gitUrl,repoName,applicationName);
				if(!validateUserPatstatus.is2xxSuccessful()) {
					MessageDescription msg = new MessageDescription();
					List<MessageDescription> errorMessage = new ArrayList<>();
					msg.setMessage("Unexpected error occured while validating PID onboarding for the given git repo. Please try again.");
					errorMessage.add(msg);
					responseMessage.addErrors(msg);
					responseMessage.setSuccess("FAILED");
					responseMessage.setErrors(errorMessage);
					return responseMessage;
				}
			}
			catch(Exception e)
			{
				MessageDescription msg = new MessageDescription();
				List<MessageDescription> errorMessage = new ArrayList<>();
				msg.setMessage("Unexpected error occured while validating PID onboarding for the given git repo.");
				errorMessage.add(msg);
				responseMessage.addErrors(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errorMessage);
			}
			return responseMessage;
	}

	@Override
	@Transactional
	public RecipeVO getByRecipeName(String recipeName) {
		CodeServerRecipeNsql entity = workspaceCustomRecipeRepo.findByRecipeName(recipeName);
		return recipeAssembler.toVo(entity);
	}

	@Override
	@Transactional
	public List<SoftwareCollection> getAllsoftwareLov()
	{
		List<CodeServerSoftwareNsql> allSoftwares = workspaceCustomSoftwareRepo.findAllSoftwareDetails();
		if(!allSoftwares.isEmpty() || allSoftwares.size()>0)
		{
			return allSoftwares.stream().map(n-> softwareAssembler.toVo(n)).collect(Collectors.toList());
		}
		else
		{
			log.info("there are no records of software ");
		}
		return null;
	}

	@Override
	@Transactional
	public List<AdditionalServiceLovVo> getAllAdditionalServiceLov() {
		List<AdditionalServiceLovVo> additionalServiceLovVo = new ArrayList<>();
		try{
			List<CodeServerAdditionalServiceNsql> allServices = additionalServiceRepo.findAllServices(10, 0);
			if(!allServices.isEmpty() || allServices.size() >0)
			{
				additionalServiceLovVo = allServices.stream().map(n-> additionalServiceAssembler.toVo(n)).collect(Collectors.toList());
				log.info("Additional Services fetched successfully");
			}
			else
			{
				log.info("Additional Services not available");
				return null;
			}
		}
		catch(Exception e)
		{
			log.error("failed while fetching additional properties",e);
		}
		return additionalServiceLovVo ;
	}

	@Override
	@Transactional
	public List<RecipeLovVO> getAllRecipeLov(String id)
	{
		List<CodeServerRecipeDto> publiclovDeatils = workspaceCustomRecipeRepo.getAllPublicRecipeLov();
		List<CodeServerRecipeDto> privatelovDetails =  workspaceCustomRecipeRepo.getAllPrivateRecipeLov(id);
		if(privatelovDetails!=null)
		{
			publiclovDeatils.addAll(privatelovDetails);
		}
		if(publiclovDeatils!=null)
		{
			return publiclovDeatils.stream().map(n-> recipeAssembler.toRecipeLovVO(n)).collect(Collectors.toList());
		}
		else
		{
			log.info("there are no recipe lov details ");
		}
		return null;

	}

	// @Override
	// @Transactional
	// public List<RecipeVO> getAllRecipesWhichAreInRequestedAndAcceptedState(int offset, int limit){
    //     List<CodeServerRecipeNsql> entities = workspaceCustomRecipeRepo.findAllRecipesWithRequestedAndAcceptedState(offset, limit);
    //     return entities.stream().map(n -> recipeAssembler.toVo(n)).collect(Collectors.toList());
    // }

	// @Override
	// @Transactional
	// public GenericMessage saveRecipeInfo(String name)
	// {
	// 	GenericMessage responseMessage = new GenericMessage();
	// 	if(name != null)
	// 	{
	// 		try
	// 		{
	// 			responseMessage = workspaceCustomRecipeRepo.updateRecipeInfo(name,"ACCEPTED");
	// 		}
	// 		catch(Exception e)
	// 		{
	// 			log.info("failed in recipe service while updating status to accept state",e.getMessage());
	// 			log.error("caught exception while changing status {}", e.getMessage());
	// 			MessageDescription msg = new MessageDescription();
	// 			List<MessageDescription> errorMessage = new ArrayList<>();
	// 			msg.setMessage("caught exception while saving recipe info");
	// 			errorMessage.add(msg);
	// 			responseMessage.addErrors(msg);
	// 			responseMessage.setSuccess("FAILED");
	// 			responseMessage.setErrors(errorMessage);
	// 		}
	
	// 		CodeServerRecipeNsql entity = workspaceCustomRecipeRepo.findByRecipeName(name);
	// 		RecipeVO data =  recipeAssembler.toVo(entity);
	// 		UserInfoVO vo = data.getCreatedBy();
	
	// 		CreatedByVO currentUser = this.userStore.getVO();
	// 		String userId = currentUser != null ? currentUser.getId() : null;
	
	// 		String resourceID = name;
	// 		List<String> teamMembers = new ArrayList<>();
	// 		List<String> teamMembersEmails = new ArrayList<>();
	// 		List<ChangeLogVO> changeLogs = new ArrayList<>();
	// 		UserInfoVO projectOwner = vo;
	// 		teamMembers.add(projectOwner.getId());
	// 		teamMembersEmails.add(projectOwner.getEmail());
	
	// 		String eventType = "Codespace-Recipe Status Update";
	// 		String message = ""; 
	
	// 		message = "Codespace Recipe " + data.getRecipeName() + " is accepted by Codespace Admin.";
	// 		kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
	// 	}
	// 	else
	// 	{
	// 		log.info("Failed in recipe service method during accept...");
	// 	}
	// 	return responseMessage;

	// }

	// @Override
	// @Transactional
	// public GenericMessage publishRecipeInfo(String name)
	// {
	// 	GenericMessage responseMessage = new GenericMessage();
	// 	if(name != null)
	// 	{
	// 		try
	// 		{
				
	// 			responseMessage = workspaceCustomRecipeRepo.updateRecipeInfo(name,"PUBLISHED");
	// 		}
	// 		catch(Exception e)
	// 		{
	// 			log.info("failed in recipe service while updating status to publish state",e.getMessage());
	// 			log.error("caught exception while changing status {}", e.getMessage());
	// 			MessageDescription msg = new MessageDescription();
	// 			List<MessageDescription> errorMessage = new ArrayList<>();
	// 			msg.setMessage("caught exception while saving recipe info");
	// 			errorMessage.add(msg);
	// 			responseMessage.addErrors(msg);
	// 			responseMessage.setSuccess("FAILED");
	// 			responseMessage.setErrors(errorMessage);
	// 		}
	
	// 		CodeServerRecipeNsql entity = workspaceCustomRecipeRepo.findByRecipeName(name);
	// 		RecipeVO data =  recipeAssembler.toVo(entity);
	// 		UserInfoVO vo = data.getCreatedBy();
	
	// 		CreatedByVO currentUser = this.userStore.getVO();
	// 		String userId = currentUser != null ? currentUser.getId() : null;
	
	// 		String resourceID = name;
	// 		List<String> teamMembers = new ArrayList<>();
	// 		List<String> teamMembersEmails = new ArrayList<>();
	// 		List<ChangeLogVO> changeLogs = new ArrayList<>();
	// 		UserInfoVO projectOwner = vo;
	// 		teamMembers.add(projectOwner.getId());
	// 		teamMembersEmails.add(projectOwner.getEmail());
	
	// 		String eventType = "Codespace-Recipe Status Update";
	// 		String message = ""; 
	
	// 		message = "Codespace Recipe " + data.getRecipeName() + " is published by Codespace Admin.";
	// 		kafkaProducer.send(eventType, resourceID, "", userId, message, true, teamMembers, teamMembersEmails, null);
	// 	}
	// 	else
	// 	{
	// 		log.info("Failed in recipe service method during publish...");
	// 	}
	// 	return responseMessage;

	// }

	@Override
	public GenericMessage deleteRecipe(String recipeName)
	{
		GenericMessage msg = new GenericMessage();
		CodeServerRecipeNsql recipe = workspaceCustomRecipeRepo.findByRecipeName(recipeName);
        if (recipe != null) {
			GenericMessage val =   workspaceCustomRecipeRepo.deleteRecipe(recipe);
            return new GenericMessage("Recipe deleted successfully");
        } else {
            return new GenericMessage("Recipe not found");
        }

	}
}