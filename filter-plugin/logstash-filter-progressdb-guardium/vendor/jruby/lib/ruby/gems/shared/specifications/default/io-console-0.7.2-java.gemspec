# -*- encoding: utf-8 -*-
# stub: io-console 0.7.2 java lib/ffi lib

Gem::Specification.new do |s|
  s.name = "io-console".freeze
  s.version = "0.7.2"
  s.platform = "java".freeze

  s.required_rubygems_version = Gem::Requirement.new(">= 0".freeze) if s.respond_to? :required_rubygems_version=
  s.metadata = { "changelog_uri" => "https://github.com/ruby/io-console/releases", "source_code_url" => "https://github.com/ruby/io-console" } if s.respond_to? :metadata=
  s.require_paths = ["lib/ffi".freeze, "lib".freeze]
  s.authors = ["Nobu Nakada".freeze]
  s.date = "2024-01-18"
  s.description = "add console capabilities to IO instances.".freeze
  s.email = "nobu@ruby-lang.org".freeze
  s.files = [".document".freeze, "LICENSE.txt".freeze, "README.md".freeze, "lib/ffi/io/console.rb".freeze, "lib/ffi/io/console/bsd_console.rb".freeze, "lib/ffi/io/console/common.rb".freeze, "lib/ffi/io/console/linux_console.rb".freeze, "lib/ffi/io/console/native_console.rb".freeze, "lib/ffi/io/console/stty_console.rb".freeze, "lib/ffi/io/console/stub_console.rb".freeze, "lib/ffi/io/console/version.rb".freeze, "lib/io/console/size.rb".freeze]
  s.homepage = "https://github.com/ruby/io-console".freeze
  s.licenses = ["Ruby".freeze, "BSD-2-Clause".freeze]
  s.required_ruby_version = Gem::Requirement.new(">= 2.6.0".freeze)
  s.rubygems_version = "3.3.26".freeze
  s.summary = "Console interface".freeze
end
