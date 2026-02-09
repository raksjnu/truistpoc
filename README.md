# Added [not(parent::*[local-name()='parent']) and not(parent::*[local-name()='project'])] to exclude parent and main project version
          xpath: "//*[local-name()='version'][not(parent::*[local-name()='parent']) and not(parent::*[local-name()='project']) and not(contains(., '${')) and normalize-space(.) != '']"
