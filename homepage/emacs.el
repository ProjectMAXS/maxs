(if (not (locate-library "org"))
    (require 'package)
    (package-initialize t)
    (package-activate "org" 0))

(require 'org-publish)  

(setq org-publish-project-alist
      '(
	("documentation"
	 :base-directory "../documentation"
	 :publishing-directory "/ssh:mate.geekplace.eu:/var/vhosts/projectmaxs.org/documentation"
	 :publishing-function org-html-publish-to-html
	 :html-head "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://thomasf.github.io/solarized-css/solarized-light.min.css\" />"
	 :infojs-opt "view:showall toc:nil ltoc:nil mouse:underline buttons:t path:http://thomasf.github.io/solarized-css/org-info.min.js"
	 )
	("maxs"
	 :base-directory "."
	 :publishing-directory "/ssh:mate.geekplace.eu:/var/vhosts/projectmaxs.org/homepage"
	 :publishing-function org-html-publish-to-html
	 :html-head "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://thomasf.github.io/solarized-css/solarized-light.min.css\" />"
	 :infojs-opt "view:showall toc:nil ltoc:nil mouse:underline buttons:t path:http://thomasf.github.io/solarized-css/org-info.min.js"
	 )
	))

