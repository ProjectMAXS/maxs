(if (not (locate-library "org"))
    (require 'package)
    (package-initialize t)
    (package-activate "org" 0))

(require 'org-publish)  

(setq org-publish-project-alist
      '(("maxs"
	 :base-directory "."
	 :publishing-directory "/ssh:mate.geekplace.eu:/var/vhosts/projectmaxs.org"
	 :publishing-function org-html-publish-to-html
	 )))

