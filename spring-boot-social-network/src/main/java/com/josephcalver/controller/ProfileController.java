package com.josephcalver.controller;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.validation.Valid;

import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.josephcalver.exceptions.ImageTooSmallException;
import com.josephcalver.exceptions.InvalidFileException;
import com.josephcalver.model.dto.FileInfo;
import com.josephcalver.model.dto.SearchResult;
import com.josephcalver.model.entity.Interest;
import com.josephcalver.model.entity.Profile;
import com.josephcalver.model.entity.SiteUser;
import com.josephcalver.service.FileService;
import com.josephcalver.service.InterestService;
import com.josephcalver.service.ProfileService;
import com.josephcalver.service.SiteUserService;
import com.josephcalver.status.PhotoUploadStatus;

@Controller
public class ProfileController {

	@Autowired
	private SiteUserService siteUserService;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private FileService fileService;

	@Autowired
	private InterestService interestService;

	@Autowired
	private PolicyFactory htmlPolicy;

	@Value(value = "${photo.upload.directory}")
	private String photoUploadDirectory;

	@Value(value = "${photo.upload.ok}")
	private String photoStatusOK;

	@Value(value = "${photo.upload.invalid}")
	private String photoStatusInvalid;

	@Value(value = "${photo.upload.ioexception}")
	private String photoStatusIOException;

	@Value(value = "${photo.upload.toosmall}")
	private String photoStatusTooSmall;

	private SiteUser getUser() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		return siteUserService.get(email);
	}

	private ModelAndView showProfile(SiteUser user) {

		ModelAndView modelAndView = new ModelAndView();

		if (user == null) {
			modelAndView.setViewName("redirect:/");
			return modelAndView;
		}

		Profile profile = profileService.getUserProfile(user);

		if (profile == null) {
			profile = new Profile();
			profile.setUser(user);
			profileService.save(profile);
		}

		Profile webProfile = new Profile();
		webProfile.safeCopyFrom(profile);

		SiteUser webUser = new SiteUser();
		webUser.safeCopyFrom(user);

		modelAndView.getModel().put("profile", webProfile);
		modelAndView.getModel().put("user", webUser);

		modelAndView.setViewName("profile");

		return modelAndView;
	}

	@RequestMapping(value = "/profile")
	ModelAndView showProfile() {

		SiteUser user = getUser();

		ModelAndView modelAndView = showProfile(user);

		modelAndView.getModel().put("user", user);
		modelAndView.getModel().put("ownProfile", true);

		return modelAndView;
	}

	@RequestMapping(value = "/profile/{id}")
	ModelAndView showProfile(@PathVariable("id") Long id) {

		SiteUser user = siteUserService.get(id);

		ModelAndView modelAndView = showProfile(user);

		modelAndView.getModel().put("ownProfile", false);

		return modelAndView;
	}

	@RequestMapping(value = "/edit-profile", method = RequestMethod.GET)
	ModelAndView editProfile(ModelAndView modelAndView) {

		SiteUser user = getUser();
		Profile profile = profileService.getUserProfile(user);

		Profile webProfile = new Profile();
		webProfile.safeCopyFrom(profile);

		modelAndView.getModel().put("profile", webProfile);
		modelAndView.setViewName("edit-profile");

		return modelAndView;
	}

	@RequestMapping(value = "/edit-profile", method = RequestMethod.POST)
	ModelAndView editProfile(ModelAndView modelAndView, @Valid Profile webProfile, BindingResult result) {

		modelAndView.setViewName("edit-profile");

		SiteUser user = getUser();
		Profile profile = profileService.getUserProfile(user);

		profile.safeMergeWith(webProfile, htmlPolicy);

		if (!result.hasErrors()) {
			profileService.save(profile);
			modelAndView.setViewName("redirect:/profile");
		}

		return modelAndView;
	}

	@RequestMapping(value = "/upload-profile-photo", method = RequestMethod.POST)
	@ResponseBody // Return data in JSON format (default type for @ResponseBody)
	ResponseEntity<PhotoUploadStatus> handlePhotoUploads(@RequestParam("file") MultipartFile file) {

		SiteUser user = getUser();
		Profile profile = profileService.getUserProfile(user);

		Path oldPhotoPath = profile.getPhoto(photoUploadDirectory);

		PhotoUploadStatus status = new PhotoUploadStatus(photoStatusOK);

		try {
			FileInfo photoInfo = fileService.saveImageFile(file, photoUploadDirectory, "photos", "p" + user.getId(),
					100, 100);

			profile.setPhotoDetails(photoInfo);
			profileService.save(profile);

			if (oldPhotoPath != null) {
				Files.delete(oldPhotoPath);
			}

		} catch (InvalidFileException e) {
			status.setMessage(photoStatusInvalid);
			e.printStackTrace();
		} catch (IOException e) {
			status.setMessage(photoStatusIOException);
			e.printStackTrace();
		} catch (ImageTooSmallException e) {
			status.setMessage(photoStatusTooSmall);
			e.printStackTrace();
		}

		return new ResponseEntity<>(status, HttpStatus.OK);
	}

	@RequestMapping(value = "/profile-photo/{id}", method = RequestMethod.GET)
	@ResponseBody
	ResponseEntity<InputStreamResource> servePhoto(@PathVariable("id") Long id) throws IOException {

		SiteUser user = siteUserService.get(id);
		Profile profile = profileService.getUserProfile(user);

		Path photoPath = Paths.get(photoUploadDirectory, "default", "avatar.jpg");

		if (profile != null && profile.getPhoto(photoUploadDirectory) != null) {
			photoPath = profile.getPhoto(photoUploadDirectory);
		}

		return ResponseEntity.ok().contentLength(Files.size(photoPath))
				.contentType(MediaType.parseMediaType(URLConnection.guessContentTypeFromName(photoPath.toString())))
				.body(new InputStreamResource(Files.newInputStream(photoPath, StandardOpenOption.READ)));
	}

	@RequestMapping(value = "/save-interest", method = RequestMethod.POST)
	@ResponseBody
	ResponseEntity<?> saveInterest(@RequestParam("name") String interestName) {

		SiteUser user = getUser();
		Profile profile = profileService.getUserProfile(user);

		String sanitizedInterestName = htmlPolicy.sanitize(interestName);

		Interest interest = interestService.createIfNotExists(sanitizedInterestName);

		profile.addInterest(interest);
		profileService.save(profile);

		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	@RequestMapping(value = "/delete-interest", method = RequestMethod.POST)
	@ResponseBody
	ResponseEntity<?> deleteInterest(@RequestParam("name") String interestName) {

		SiteUser user = getUser();
		Profile profile = profileService.getUserProfile(user);

		profile.removeInterest(interestName);

		profileService.save(profile);

		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	@RequestMapping(value = "/profiles", method = RequestMethod.GET)
	ModelAndView showAllUserProfiles(ModelAndView modelAndView,
			@RequestParam(name = "p", defaultValue = "1") int pageNumber) {

		Page<SearchResult> page = profileService.findAllProfiles(pageNumber);

		modelAndView.getModel().put("page", page);
		modelAndView.setViewName("profiles");

		return modelAndView;
	}

}
