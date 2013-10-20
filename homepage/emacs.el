(if (not (locate-library "org"))
    (require 'package)
    (package-initialize t)
    (package-activate "org" 0))

(require 'org-publish)  

(defconst my-html-head
"<link rel=\"stylesheet\" type=\"text/css\" href=\"/homepage/zenburn.min.css\" />
<script type=\"text/javascript\">
var _paq = _paq || [];
_paq.push([\"trackPageView\"]);
_paq.push([\"enableLinkTracking\"]);
(function() {
var u=((\"https:\" == document.location.protocol) ? \"https\" : \"http\") + \"://piwik.geekplace.eu/\";
_paq.push([\"setTrackerUrl\", u+\"piwik.php\"]);
_paq.push([\"setSiteId\", \"1\"]);
var d=document, g=d.createElement(\"script\"), s=d.getElementsByTagName(\"script\")[0]; g.type=\"text/javascript\";
g.defer=true; g.async=true; g.src=u+\"piwik.js\"; s.parentNode.insertBefore(g,s);
})();
</script>")

(setq org-html-head my-html-head)

(setq org-publish-project-alist
      '(
	("documentation"
	 :base-directory "../documentation"
	 :publishing-directory "/ssh:mate.geekplace.eu:/var/vhosts/projectmaxs.org/documentation"
	 :publishing-function org-html-publish-to-html
	 :infojs-opt "view:showall toc:nil ltoc:nil mouse:underline buttons:t path:/homepage/org-info.js"
	 :auto-sitemap t
	 :sitemap-sort-files "alphabetically"
	 :sitemap-title "MAXS Documentation"
	 )
	("maxs-static"
	 :base-directory "."
	 :base-extension "css\\|js\\|png"
	 :publishing-directory "/ssh:mate.geekplace.eu:/var/vhosts/projectmaxs.org/homepage"
	 :publishing-function org-publish-attachment
	 )
	("maxs"
	 :base-directory "."
	 :publishing-directory "/ssh:mate.geekplace.eu:/var/vhosts/projectmaxs.org/homepage"
	 :publishing-function org-html-publish-to-html
	 :infojs-opt "view:showall toc:nil ltoc:nil mouse:underline buttons:t path:/homepage/org-info.js"
	 )
	))

