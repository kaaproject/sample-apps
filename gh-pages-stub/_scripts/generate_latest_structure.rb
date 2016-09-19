require 'yaml'
require "fileutils"

CONFIG_DATA = YAML.load_file("_data/generated_config.yml")
DOCS_ROOT = CONFIG_DATA["docs_root"]
LATEST_VERSION = CONFIG_DATA["version"]
LATEST_DIR = "#{DOCS_ROOT}/latest"
REDIRECT_CONTENT = <<-eos
---
layout: redirected
sitemap: false
redirect_to: $LATEST
---
eos

FileUtils.rm_f(LATEST_DIR)
FileUtils.cp_r("#{DOCS_ROOT}/#{LATEST_VERSION}/", LATEST_DIR)
Dir.glob("#{LATEST_DIR}/**/index.md").each { |file_name|
	puts file_name
	File.truncate(file_name,0)
	file = File.new(file_name,"w")
	file.write(REDIRECT_CONTENT)
}